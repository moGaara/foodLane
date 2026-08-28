package com.app.foodlane.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDto(Long cartId,
                              Long restaurantId,
                              String restaurantName,
                              List<CartItemResponseDto> items,
                              BigDecimal subtotal) {

}
