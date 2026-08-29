package com.app.foodlane.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CartResponse {
    private Long cartId;
    private Long customerId;
    private Long restaurantId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
}
