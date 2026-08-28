package com.app.foodlane.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    private Long cartItemId;

    private Long cartId;

    private Long menuItemId;

    private Integer quantity;

    private BigDecimal unitPriceSnapshot;

    private String itemNote;
}