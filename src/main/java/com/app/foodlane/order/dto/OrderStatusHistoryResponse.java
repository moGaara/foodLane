package com.app.foodlane.order.dto;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(String status,
        LocalDateTime changedAt) {

}
