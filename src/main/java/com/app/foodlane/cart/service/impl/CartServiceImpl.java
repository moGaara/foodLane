package com.app.foodlane.cart.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.foodlane.cart.dto.request.CartItemCustomizationRequest;
import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartItemCustomization;
import com.app.foodlane.cart.mapper.CartMapper;
import com.app.foodlane.cart.repository.CartItemCustomizationRepository;
import com.app.foodlane.cart.repository.CartItemRepository;
import com.app.foodlane.cart.repository.CustomizationOptionRepository;
import com.app.foodlane.cart.service.UpdateCartService;
import com.app.foodlane.restaurant.entity.CustomizationGroup;
import com.app.foodlane.restaurant.entity.CustomizationOption;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.exceptionhandling.BusinessException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements UpdateCartService {
    private final CartItemRepository cartItemRepository;
    private final CartItemCustomizationRepository cartItemCustomizationRepository;
    private final CustomizationOptionRepository customizationOptionRepository;
    private final CartMapper cartMapper;
    /**
     * Applies PATCH semantics to quantity, note, and customizations.
     */
    @Override
    @Transactional
    public CartResponse updateCartItem(
            Long cartId,
            Long cartItemId,
            Long customerId,
            UpdateCartItemRequest request) {

        log.debug("Updating cart item: cartId={}, cartItemId={}, customerId={}",
                cartId, cartItemId, customerId);

        CartItem cartItem = cartItemRepository
                .findByCartItemIdAndCartCartIdAndCartCustomerCustomerIdAndCartStatus(
                        cartItemId, cartId, customerId, "ACTIVE")
                .orElseThrow(() -> new BusinessException(ErrorMapping.CART_ITEM_NOT_EXIST));

        Cart cart = cartItem.getCart();

        // Quantity zero means delete; zero is never persisted because the DB allows 1-99.
        if (request.getQuantity() != null && request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            cartItemRepository.flush();
            log.info("Cart item deleted: cartId={}, cartItemId={}", cartId, cartItemId);
            return cartMapper.toCartResponse(cart);
        }

        if (request.getQuantity() != null) {
            validateInventory(cartItem, request.getQuantity());
            cartItem.setQuantity(request.getQuantity());
        }

        if (request.getItemNote() != null) {
            cartItem.setItemNote(request.getItemNote());
        }

        if (request.getCustomizations() != null) {
            replaceCustomizations(cartItem, request.getCustomizations());
        }

        cartItemRepository.save(cartItem);
        log.info("Cart item updated: cartId={}, cartItemId={}, quantity={}",
                cartId, cartItemId, cartItem.getQuantity());
        return cartMapper.toCartResponse(cart);
    }

    private void validateInventory(CartItem cartItem, Integer quantity) {
        if (quantity > cartItem.getMenuItem().getInventoryQuantity()) {
            log.warn("Rejected unavailable quantity: cartItemId={}, requested={}, available={}",
                    cartItem.getCartItemId(), quantity,
                    cartItem.getMenuItem().getInventoryQuantity());
            throw new BusinessException(ErrorMapping.INSUFFICIENT_INVENTORY);
        }
    }

    private void replaceCustomizations(
            CartItem cartItem,
            List<CartItemCustomizationRequest> requests) {

        // Duplicate selections would violate the cart-item/option unique constraint.
        Set<Long> requestedOptionIds = requests.stream()
                .map(CartItemCustomizationRequest::getCustomizationOptionId)
                .collect(Collectors.toCollection(HashSet::new));

        if (requestedOptionIds.size() != requests.size()) {
            log.warn("Rejected duplicate customizations: cartItemId={}", cartItem.getCartItemId());
            throw new BusinessException(ErrorMapping.DUPLICATE_CUSTOMIZATION);
        }

        Map<Long, CustomizationOption> options = customizationOptionRepository
                .findAllById(requestedOptionIds)
                .stream()
                .collect(Collectors.toMap(
                        CustomizationOption::getCustomizationOptionId,
                        Function.identity()));

        if (options.size() != requestedOptionIds.size()) {
            log.warn("Rejected unknown customization option: cartItemId={}",
                    cartItem.getCartItemId());
            throw new BusinessException(ErrorMapping.CUSTOMIZATION_NOT_EXIST);
        }

        // Only groups assigned to the selected menu item may be used.
        Map<Long, CustomizationGroup> allowedGroups = cartItem.getMenuItem()
                .getCustomizationGroups()
                .stream()
                .collect(Collectors.toMap(
                        CustomizationGroup::getCustomizationGroupId,
                        Function.identity()));

        // Quantity zero removes an option, so only positive selections are persisted.
        List<CartItemCustomizationRequest> activeRequests = requests.stream()
                .filter(request -> request.getQuantity() > 0)
                .toList();

        Map<Long, Integer> selectionsPerGroup = new HashMap<>();
        for (CartItemCustomizationRequest request : activeRequests) {
            CustomizationOption option = options.get(request.getCustomizationOptionId());
            Long groupId = option.getCustomizationGroup().getCustomizationGroupId();
            if (!allowedGroups.containsKey(groupId)) {
                log.warn("Rejected unavailable customization: cartItemId={}, optionId={}",
                        cartItem.getCartItemId(), option.getCustomizationOptionId());
                throw new BusinessException(ErrorMapping.CUSTOMIZATION_NOT_ALLOWED);
            }
            selectionsPerGroup.merge(groupId, 1, Integer::sum);
        }

        // Enforce each group's minimum and maximum number of selected options.
        for (CustomizationGroup group : allowedGroups.values()) {
            int selected = selectionsPerGroup.getOrDefault(group.getCustomizationGroupId(), 0);
            if (selected < group.getMinSelect() || selected > group.getMaxSelect()) {
                log.warn(
                        "Rejected customization count: cartItemId={}, groupId={}, selected={}, min={}, max={}",
                        cartItem.getCartItemId(), group.getCustomizationGroupId(), selected,
                        group.getMinSelect(), group.getMaxSelect());
                throw new BusinessException(ErrorMapping.INVALID_CUSTOMIZATION_SELECTION);
            }
        }

        // A supplied list replaces all previous selections; omitted lists are left unchanged.
        cartItemCustomizationRepository.deleteAllByCartItemCartItemId(cartItem.getCartItemId());
        cartItemCustomizationRepository.flush();

        List<CartItemCustomization> replacements = activeRequests.stream()
                .map(request -> {
                    CustomizationOption option = options.get(request.getCustomizationOptionId());
                    return CartItemCustomization.builder()
                            .cartItem(cartItem)
                            .customizationOption(option)
                            // Never trust a client-provided price; snapshot the current DB price.
                            .priceSnapshot(option.getPrice())
                            .quantity(request.getQuantity())
                            .build();
                })
                .toList();

        cartItemCustomizationRepository.saveAll(replacements);
        log.debug("Cart customizations replaced: cartItemId={}, activeSelections={}",
                cartItem.getCartItemId(), replacements.size());
    }

}
