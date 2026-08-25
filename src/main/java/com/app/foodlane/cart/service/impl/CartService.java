package com.app.foodlane.cart.service.impl;

import com.app.foodlane.Auth.entity.Customer;
import com.app.foodlane.Auth.service.CustomerService;
import com.app.foodlane.cart.dto.request.AddToCartRequesttDto;
import com.app.foodlane.cart.dto.request.CustomizationSelectionDto;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartItemCustomization;
import com.app.foodlane.cart.entity.CartStatus;
import com.app.foodlane.cart.repository.CartRepository;
import com.app.foodlane.restaurant.dto.DbCustomizationItem;
import com.app.foodlane.restaurant.entity.CustomizationGroup;
import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.restaurant.entity.MenuItem;
import com.app.foodlane.restaurant.entity.Restaurant;
import com.app.foodlane.restaurant.service.CustomizationOptionService;
import com.app.foodlane.restaurant.service.MenuItemService;
import com.app.foodlane.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final MenuItemService menuItemService;
    private final RestaurantService restaurantService;
    private final CustomerService customerService;
    private final CartItemService cartItemService;
    private final CustomizationOptionService customizationOptionService;
    private final CartItemCustomizationService cartItemCustomizationService;
    Map<Long, DbCustomizationItem> dbCustomizationItemMap = new HashMap<>();

    public CartResponseDto addItem(long customerId, AddToCartRequesttDto requestDto) {
        // get current restaurant
        Restaurant restaurant = restaurantService.getById(requestDto.restaurantId());
        // get current customer
        Customer customer = customerService.getById(customerId);
        // get current menuItem
        MenuItem menuItem = menuItemService.getById(requestDto.menuItemId());
        // check if menuItem belongs to current restaurant
        boolean identicalRestaurant = haveIdenticalRestaurant(menuItem, requestDto.restaurantId());
        // check menu item inventory quantity verse customer request quantity
        boolean isAvailable = hasAvailableQuantity(menuItem.getInventoryQuantity(), requestDto.menuItemQuantity());
        // get customizations of current menuItem
        /*
         * get optionGroup of menuItem
         * get options of each group
         * compare required group's optionItem verse request customizations required?
         * compare optional group's optionItem verse request customizations max/min
         * i created map has customizationItemId & its needed info
         * */
        getMenuItemCustomizationGroup(menuItem);

        // check is there active cart?
        Cart activeCart = returnActiveCartOrNull(customerId);
        if (activeCart != null) {
            if (isAvailable) {
                if (identicalRestaurant) {
                    // check customization => if valid customization
                    // compare request customization against existed in db
                    if (isValidCustomization(dbCustomizationItemMap, requestDto.customizationSelectionDtoList())) {
                        // check if the menuItem already exist in cart
                        Optional<CartItem> activeCartItem = activeCart.getCartItemsList().stream()
                                .filter(cartItem ->
                                        Objects.equals(cartItem.getMenuItem().getMenuItemId(),
                                                requestDto.menuItemId()))
                                .findFirst();
                        // check if customizations are identical => increase quantity
                        boolean isIdentical = areCustomizationIdentical
                                (requestDto.customizationSelectionDtoList()
                                        , activeCartItem.get().getCartItemCustomizations());
                        if (isIdentical) {
                            activeCartItem.get().setQuantity(activeCartItem.get().getQuantity() + 1);
                            createCartItemCustomization(requestDto, activeCartItem.get());
                        } else {
                            CartItem cartItem = createCartItem(activeCart, requestDto, menuItem);
                            createCartItemCustomization(requestDto, cartItem);
                        }
                    }
                } else {
                    // let client display a pop-up to customer to choose in which path he will continue
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_CONTENT, "restaurant not identical");
                }
            } else {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_CONTENT, "item exists but not available");
            }

        } else {
            if (isAvailable && identicalRestaurant) {
                // create new cart
                Cart savedCart = createCart(customer, restaurant);
                // create new cartItem
                CartItem savedCartItem = createCartItem(savedCart, requestDto, menuItem);
                // create customization then assign it to cartItem
                List<CartItemCustomization> cartItemCustomization = createCartItemCustomization(requestDto, savedCartItem);
            }

        }
        return new CartResponseDto();
    }

    private Cart returnActiveCartOrNull(Long customerId) {
        return cartRepository.findByStatusAndCustomerCustomerId
                        (CartStatus.ACTIVE.toString(), customerId)
                .orElseGet(null);
    }

    private boolean haveIdenticalRestaurant(MenuItem menuItem, Long restaurantId) {
        return Objects.equals(menuItem.getCategory().getMenu()
                .getRestaurant().getRestaurantId(), restaurantId);
    }

    private boolean hasAvailableQuantity(Integer inventoryQuantity, Integer requestQuantity) {
        return requestQuantity <= inventoryQuantity;
    }

    private Cart saveEntity(Cart cart) {
        return cartRepository.save(cart);
    }

    private void getMenuItemCustomizationGroup(MenuItem menuItem) {
        Set<CustomizationGroup> groups = menuItem.getCustomizationGroups();
        getCustomizationItems(groups);
    }

    private void getCustomizationItems(Set<CustomizationGroup> groups) {
        DbCustomizationItem dbCustomizationItem;
        for (CustomizationGroup group : groups) {
            List<CustomizationOption> customizationOptions = group.getCustomizationOptions();
            for (CustomizationOption item : customizationOptions) {
                dbCustomizationItem = DbCustomizationItem.builder()
                        .customizationItemId(item.getCustomizationOptionId())
                        .customizationItemSnapShot(item.getPrice())
                        .isRequired(group.getRequired())
                        .customizationItemMax(group.getMaxSelect())
                        .customizationItemMin(group.getMinSelect()).build();
                dbCustomizationItemMap.put(item.getCustomizationOptionId(), dbCustomizationItem);
            }

        }
    }

    private boolean isValidCustomization(Map<Long, DbCustomizationItem> dbCustomizationItemMap,
                                         List<CustomizationSelectionDto> customizationSelectionDtos) {
        boolean isValid = true;
        for (Map.Entry<Long, DbCustomizationItem> entry : dbCustomizationItemMap.entrySet()) {
            for (CustomizationSelectionDto item : customizationSelectionDtos) {
                if (entry.getValue().isRequired()) {
                    if (!(item.quantity() == 1)) {
                        isValid = false;
                        new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                                "required customization must be selected");
                    }
                } else {
                    if (!(item.quantity() <= 6 && item.quantity() >= 0)) {
                        isValid = false;
                        new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                                "optional customization must be less than 6 pieces");
                    }
                }
            }
        }
        return isValid;
    }

    private Cart createCart(Customer customer, Restaurant restaurant) {
        Cart cart = Cart.builder()
                .restaurant(restaurant)
                .customer(customer)
                .build();
        // saved cart entity
        return this.saveEntity(cart);
    }

    private CartItem createCartItem(Cart savedCart, AddToCartRequesttDto requestDto, MenuItem menuItem) {
        CartItem cartItem = CartItem.builder()
                .itemNote(requestDto.menuItemNote())
                .quantity(requestDto.menuItemQuantity())
                .cart(savedCart)
                .unitPriceSnapshot(menuItem.getPrice())
                .menuItem(menuItem)
                .build();
        // saved cartItem entity
        return cartItemService.saveEntity(cartItem);
    }

    private List<CartItemCustomization> createCartItemCustomization(AddToCartRequesttDto requestDto, CartItem savedCartItem) {
        List<CartItemCustomization> cartItemCustomizationList = new ArrayList<>();
        for (CustomizationSelectionDto customization : requestDto.customizationSelectionDtoList()) {
            CustomizationOption customizationOption = customizationOptionService
                    .getById(customization.customizationOptionId());
            CartItemCustomization cartItemCustomization = CartItemCustomization.builder()
                    .cartItemCustomizationId(customization.customizationOptionId())
                    .cartItem(savedCartItem)
                    .customizationOption(customizationOption)
                    .priceSnapshot(customizationOption.getPrice())
                    .quantity(customization.quantity())
                    .build();
            cartItemCustomizationService.saveEntity(cartItemCustomization);
        }
        return cartItemCustomizationList;
    }

    private boolean areCustomizationIdentical(List<CustomizationSelectionDto> customizationSelectionDtosList,
                                              List<CartItemCustomization> cartItemCustomizationsList) {
        boolean isIdentical = true;
        if (customizationSelectionDtosList.size() != cartItemCustomizationsList.size()) {
            isIdentical = false;
        } else {
            record ExistedCartCustomization(Long id, Integer quantity) {
            }
            List<ExistedCartCustomization> dtoListMapped = customizationSelectionDtosList.stream()
                    .map(dto -> new ExistedCartCustomization(dto.customizationOptionId(), dto.quantity()))
                    .sorted(Comparator.comparing(ExistedCartCustomization::id, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();
            List<ExistedCartCustomization> entityListMapped = cartItemCustomizationsList.stream()
                    .map(entity -> new ExistedCartCustomization(entity.getCartItemCustomizationId(), entity.getQuantity()))
                    .sorted(Comparator.comparing(ExistedCartCustomization::id, Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();
            isIdentical = dtoListMapped.equals(entityListMapped);
        }
        return isIdentical;
    }

}

