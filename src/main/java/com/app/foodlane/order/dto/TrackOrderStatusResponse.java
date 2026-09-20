package com.app.foodlane.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TrackOrderStatusResponse(

        Long orderId,
        String currentStatus,
        LocalDateTime estimatedDelivery,
        String deliveryInstructions,
        List<OrderStatusHistoryResponse> statusHistory

) {

}
