package com.app.foodlane.cart.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** One customization option selected for a cart item. */
@Data
public class CartItemCustomizationRequest {
    @NotNull(message = "Customization option ID is required")
    private Long customizationOptionId;

    @NotNull(message = "Customization quantity is required")
    @Min(value = 0, message = "Customization quantity cannot be negative")
    @Max(value = 6, message = "Customization quantity cannot exceed 6")
    private Integer quantity;
}
