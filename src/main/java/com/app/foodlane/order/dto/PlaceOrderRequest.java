package com.app.foodlane.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotNull(message = "Delivery address is required")
        @Positive(message = "Delivery address must be positive")
        Long deliveryAddressId,
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,
        @Size(max = 1000, message = "Delivery instructions cannot exceed 1000 characters")
        String deliveryInstructions
) {
}
