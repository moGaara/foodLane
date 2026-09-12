package com.app.foodlane.order.controller;

import com.app.foodlane.order.dto.PlaceOrderRequest;
import com.app.foodlane.order.dto.PlaceOrderResponse;
import com.app.foodlane.order.dto.UpdateOrderStatusRequest;
import com.app.foodlane.order.service.OrderService;
import com.app.foodlane.utils.CommonFunctions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody PlaceOrderRequest request) {
        long customerId = CommonFunctions.extractID(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(customerId, request));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<PlaceOrderResponse> updateOrderStatus(@PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

}
