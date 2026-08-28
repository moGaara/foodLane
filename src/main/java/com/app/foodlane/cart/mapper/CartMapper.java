package com.app.foodlane.cart.mapper;

import com.app.foodlane.cart.dto.response.CartItemCustomizationResponseDto;
import com.app.foodlane.cart.dto.response.CartItemResponseDto;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.entity.Cart;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.entity.CartItemCustomization;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class CartMapper {


      // Maps a cart to its API response and calculates the subtotal from its mapped items
    public CartResponseDto toCartResponseDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        // Treat an uninitialized item collection as an empty cart.
        List<CartItem> items = cart.getCartItemsList() == null ? Collections.emptyList() : cart.getCartItemsList();

        List<CartItemResponseDto> itemDtos = items.stream()
                .map(this::toCartItemResponseDto)
                .toList();

        // Each item DTO already includes its quantity and customization costs.
        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemResponseDto::itemTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponseDto(
                cart.getCartId(),
                cart.getRestaurant() != null ? cart.getRestaurant().getRestaurantId() : null,
                cart.getRestaurant() != null ? cart.getRestaurant().getName() : null,
                itemDtos,
                subtotal
        );
    }


    // Maps a cart item and calculates its price using the stored price snapshots
    public CartItemResponseDto toCartItemResponseDto(CartItem item) {
        if (item == null) {
            return null;
        }

        List<CartItemCustomization> customizations = item.getCartItemCustomizations() == null
                ? Collections.emptyList()
                : item.getCartItemCustomizations();

        List<CartItemCustomizationResponseDto> customizationDtos = customizations.stream()
                .map(this::toCartItemCustomizationResponseDto)
                .toList();

        // Customization prices are per selected unit and are preserved as cart-time snapshots.
        BigDecimal totalCustomizationsPrice = customizations.stream()
                .map(c -> {
                    BigDecimal price = c.getPriceSnapshot() != null ? c.getPriceSnapshot() : BigDecimal.ZERO;
                    int qty = c.getSelected() != null ? c.getSelected() : 0;
                    return price.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate one configured item's price before applying the cart item quantity.
        BigDecimal baseUnitPrice = item.getUnitPriceSnapshot() != null ? item.getUnitPriceSnapshot() : BigDecimal.ZERO;
        BigDecimal singleItemPrice = baseUnitPrice.add(totalCustomizationsPrice);

        int itemQuantity = item.getSelected() != null ? item.getSelected() : 0;
        BigDecimal itemTotalPrice = singleItemPrice.multiply(BigDecimal.valueOf(itemQuantity));

        return new CartItemResponseDto(
                item.getCartItemId(),
                item.getMenuItem() != null ? item.getMenuItem().getMenuItemId() : null,
                item.getMenuItem() != null ? item.getMenuItem().getName() : null,
                baseUnitPrice,
                itemQuantity,
                item.getItemNote(),
                itemTotalPrice,
                customizationDtos
        );
    }


    // Maps one selected customization using the price recorded when it was added to the cart
    public CartItemCustomizationResponseDto toCartItemCustomizationResponseDto(CartItemCustomization customization) {
        if (customization == null) {
            return null;
        }

        return new CartItemCustomizationResponseDto(
                customization.getCustomizationOption() != null ? customization.getCustomizationOption().getCustomizationOptionId() : null,
                customization.getCustomizationOption() != null ? customization.getCustomizationOption().getName() : null,
                customization.getPriceSnapshot() != null ? customization.getPriceSnapshot() : BigDecimal.ZERO,
                customization.getSelected() != null ? customization.getSelected() : 0
        );
    }
}
