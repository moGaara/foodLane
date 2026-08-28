package com.app.foodlane.cart.service;

import com.app.foodlane.Auth.entity.Customer;
import com.app.foodlane.Auth.service.CustomerService;
import com.app.foodlane.cart.dto.request.AddToCartRequestDto;
import com.app.foodlane.cart.dto.request.CustomizationSelectionDto;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartItemCustomization;
import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.mapper.CartMapper;
import com.app.foodlane.cart.repository.CartRepository;
import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.restaurant.entity.MenuItem;
import com.app.foodlane.restaurant.entity.Restaurant;
import com.app.foodlane.restaurant.service.CustomizationOptionService;
import com.app.foodlane.restaurant.utils.CustomizationValidator;
import com.app.foodlane.restaurant.service.MenuItemService;
import com.app.foodlane.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;
    private final CustomerService customerService;
    private final CartMapper cartMapper;

    private final CustomizationValidator customizationValidator;

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final CustomizationOptionService customizationOptionService;
    private final CartItemCustomizationService cartItemCustomizationService;


    public CartResponseDto addItem(long customerId, AddToCartRequestDto requestDto) {
        // get current restaurant
        Restaurant restaurant = restaurantService.getById(requestDto.restaurantId());
        // get current customer
        Customer customer = customerService.getById(customerId);
        // get current menuItem
        MenuItem menuItem = menuItemService.getByIdWithCustomizations(requestDto.menuItemId());


        // check if menuItem belongs to current restaurant
        validateMenuItemBelongsToRestaurant(menuItem, restaurant.getRestaurantId());
        // check menu item inventory selected verse customer request selected
        validateInventoryQuantity(menuItem.getInventoryQuantity(), requestDto.menuItemQuantity());


        // check selected customization against db data and rules
        customizationValidator.validate(menuItem, requestDto.customizationSelectionDtoList());

        Cart cart = returnActiveCartOrNewCart(customer , restaurant);
        // Validate Restaurant Match
        if (!cart.getRestaurant().getRestaurantId().equals(restaurant.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cart contains items from a different restaurant");
        }

        // Check if Item Exists in Cart
        List<CartItem> existingItemsWithSameMenu = cart.getCartItemsList().stream()
                .filter(item -> item.getMenuItem().getMenuItemId().equals(menuItem.getMenuItemId()))
                .toList();

        if (existingItemsWithSameMenu.isEmpty()) {
            // add item for the first item => not existed in the cart
            CartItem newItem = createCartItem(cart, menuItem, requestDto);
            createCartItemCustomization(requestDto , newItem);
        } else {
            // item is existed check on customizations & note
            Optional<CartItem> identicalItemOpt = existingItemsWithSameMenu.stream()
                    .filter(item -> isSameCustomizationAndNote(item, requestDto))
                    .findFirst();

            if (identicalItemOpt.isPresent()) {
                // identical customizations & note
                CartItem identicalItem = identicalItemOpt.get();
                identicalItem.setSelected(identicalItem.getSelected() + requestDto.menuItemQuantity());
            } else {
                // different customizations & note
                CartItem newItem = createCartItem(cart, menuItem, requestDto);
                createCartItemCustomization(requestDto , newItem);
            }
        }

        return cartMapper.toCartResponseDto(cart);
    }
    private void validateMenuItemBelongsToRestaurant(MenuItem menuItem, Long restaurantId) {
        if (!Objects.equals(menuItem.getCategory().getMenu().getRestaurant().getRestaurantId(),
                restaurantId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Menu item does not belong to the selected restaurant");
        }
    }

    private void validateInventoryQuantity(Integer inventoryQuantity, Integer requestQuantity) {
        if (inventoryQuantity < requestQuantity) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Requested selected is unavailable. Current inventory: " + inventoryQuantity);
        }
    }

    private Cart returnActiveCartOrNewCart(Customer customer, Restaurant restaurant) {
        return cartRepository.findByStatusAndCustomerCustomerId
                        (CartStatus.ACTIVE.toString(), customer.getCustomerId())
                .orElseGet(() -> createNewCart(customer, restaurant));
    }

    private Cart saveEntity(Cart cart) {
        return cartRepository.save(cart);
    }



    private Cart createNewCart(Customer customer, Restaurant restaurant) {
        Cart cart = Cart.builder()
                .restaurant(restaurant)
                .customer(customer)
                .build();
        // saved cart entity
        return this.saveEntity(cart);
    }

    private CartItem createCartItem(Cart existedCart , MenuItem menuItem, AddToCartRequestDto requestDto) {
        CartItem cartItem = CartItem.builder()
                .itemNote(requestDto.menuItemNote())
                .selected(requestDto.menuItemQuantity())
                .cart(existedCart)
                .unitPriceSnapshot(menuItem.getPrice())
                .menuItem(menuItem)
                .build();
        // saved cartItem entity
        CartItem savedCartItem = cartItemService.saveEntity(cartItem);
        existedCart.getCartItemsList().add(savedCartItem);
        return savedCartItem;
    }

    private List<CartItemCustomization> createCartItemCustomization(AddToCartRequestDto requestDto, CartItem savedCartItem) {
        List<CartItemCustomization> cartItemCustomizationList = new ArrayList<>();
        for (CustomizationSelectionDto customization : requestDto.customizationSelectionDtoList()) {
            CustomizationOption customizationOption = customizationOptionService
                    .getById(customization.customizationOptionId());
            CartItemCustomization cartItemCustomization = CartItemCustomization.builder()
                    .cartItem(savedCartItem)
                    .customizationOption(customizationOption)
                    .priceSnapshot(customizationOption.getPrice())
                    .selected(customization.selected())
                    .build();
            cartItemCustomizationService.saveEntity(cartItemCustomization);
        }
        List<CartItemCustomization> savedCartItemCustomizationList = cartItemCustomizationList;
        savedCartItem.setCartItemCustomizations(savedCartItemCustomizationList);
        return savedCartItemCustomizationList;
    }

    // compare between incoming cartItem and existed cartItem
    private boolean isSameCustomizationAndNote(CartItem existingItem, AddToCartRequestDto requestDto) {
        // 1. Compare Notes (Handles nulls safely)
        if (!Objects.equals(existingItem.getItemNote(), requestDto.menuItemNote())) {
            return false;
        }

        // Prepare incoming customizations list safely
        List<CustomizationSelectionDto> incomingSelections =
                requestDto.customizationSelectionDtoList() == null ? List.of() : requestDto.customizationSelectionDtoList();

        List<CartItemCustomization> existingCustomizations = existingItem.getCartItemCustomizations();

        // 2. Quick Check: Compare sizes first
        if (existingCustomizations.size() != incomingSelections.size()) {
            return false;
        }

        // 3. Map incoming selections for O(1) fast lookup: OptionId -> Quantity
        Map<Long, Integer> incomingMap = incomingSelections.stream()
                .collect(Collectors.toMap(
                        CustomizationSelectionDto::customizationOptionId,
                        CustomizationSelectionDto::selected
                ));

        // 4. Verify that every existing customization matches option ID AND selected
        return existingCustomizations.stream().allMatch(existing -> {
            Long existingOptionId = existing.getCustomizationOption().getCustomizationOptionId();
            Integer incomingQuantity = incomingMap.get(existingOptionId);

            return incomingQuantity != null && incomingQuantity.equals(existing.getSelected());
        });
    }


}

