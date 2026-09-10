package com.app.foodlane.cart.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.app.foodlane.cart.dto.response.CartItemCustomizationResponseDto;
import com.app.foodlane.cart.dto.response.CartItemResponseDto;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartItemCustomization;
import com.app.foodlane.cart.repository.CartItemCustomizationRepository;
import com.app.foodlane.cart.repository.CartItemRepository;

import lombok.RequiredArgsConstructor;

/** Shared mapper for converting cart entities into API responses. */
@Component
@RequiredArgsConstructor
public class CartMapper {
    private final CartItemRepository cartItemRepository;
    private final CartItemCustomizationRepository cartItemCustomizationRepository;

    /** Maps a cart to the response contract shared by add-cart and update-cart. */
    public CartResponseDto toCartResponseDto(Cart cart) {
        List<CartItemResponseDto> items = cartItemRepository.findAllByCartCartId(cart.getCartId())
                .stream()
                .map(this::toCartItemResponseDto)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponseDto::itemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDto(
                cart.getCartId(),
                cart.getRestaurant().getRestaurantId(),
                cart.getRestaurant().getName(),
                items,
                subtotal);
    }

    private CartItemResponseDto toCartItemResponseDto(CartItem cartItem) {
        List<CartItemCustomizationResponseDto> customizations = cartItemCustomizationRepository
                .findAllByCartItemCartItemId(cartItem.getCartItemId())
                .stream()
                .map(this::toCustomizationResponseDto)
                .toList();

        BigDecimal customizationUnitTotal = customizations.stream()
                .map(customization -> customization.priceSnapshot()
                        .multiply(BigDecimal.valueOf(customization.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal itemTotalPrice = cartItem.getUnitPriceSnapshot()
                .add(customizationUnitTotal)
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponseDto(
                cartItem.getCartItemId(),
                cartItem.getMenuItem().getMenuItemId(),
                cartItem.getMenuItem().getName(),
                cartItem.getUnitPriceSnapshot(),
                cartItem.getQuantity(),
                cartItem.getItemNote(),
                itemTotalPrice,
                customizations);
    }

    private CartItemCustomizationResponseDto toCustomizationResponseDto(
            CartItemCustomization customization) {
        return new CartItemCustomizationResponseDto(
                customization.getCustomizationOption().getCustomizationOptionId(),
                customization.getCustomizationOption().getName(),
                customization.getPriceSnapshot(),
                customization.getQuantity());
    }
}
