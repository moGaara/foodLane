package com.app.foodlane.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlaceOrderResponse(
        Long orderId,
        String status,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal deliveryFee,
        BigDecimal serviceFee,
        BigDecimal totalAmount,
        PaymentResponse payment,
        LocalDateTime placedAt
) {
}
