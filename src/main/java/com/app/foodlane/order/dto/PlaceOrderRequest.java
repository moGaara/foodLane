package com.app.foodlane.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlaceOrderRequest(
        @NotNull(message = "Delivery address is required")
        @Positive(message = "Delivery address must be positive")
        Long deliveryAddressId,
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
