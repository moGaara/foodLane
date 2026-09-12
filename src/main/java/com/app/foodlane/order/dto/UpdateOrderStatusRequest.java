package com.app.foodlane.order.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(@NotBlank(message = "Order status required") String status) {

}
