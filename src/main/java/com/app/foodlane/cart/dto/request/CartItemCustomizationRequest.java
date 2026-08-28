package com.app.foodlane.cart.dto.request;

import com.app.foodlane.utils.ErrorConstants;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** One customization option selected for a cart item. */
@Data
public class CartItemCustomizationRequest {
    @NotNull(message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    private Long customizationOptionId;

    @NotNull(message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    @Min(value = 0, message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    @Max(value = 6, message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    private Integer quantity;
}
