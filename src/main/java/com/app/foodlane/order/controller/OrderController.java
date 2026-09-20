package com.app.foodlane.order.controller;

import com.app.foodlane.Auth.entity.UserRole;
import com.app.foodlane.order.dto.PlaceOrderRequest;
import com.app.foodlane.order.dto.PlaceOrderResponse;
import com.app.foodlane.order.dto.TrackOrderStatusResponse;
import com.app.foodlane.order.dto.UpdateOrderStatusRequest;
import com.app.foodlane.order.service.OrderService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.reswrapper.GenericRes;

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
    public ResponseEntity<PlaceOrderResponse> updateOrderStatus(@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") UserRole role,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        orderId,
                        userId,
                        role,
                        request));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<GenericRes<TrackOrderStatusResponse>> trackOrderStatus(
            @RequestHeader("Authorization") String auth, @PathVariable Long orderId) {
        Long customerId = CommonFunctions.extractID(auth);
        TrackOrderStatusResponse serviceResponse = orderService.trackOrderStatus(orderId, customerId);
        GenericRes<TrackOrderStatusResponse> response = new GenericRes<>();
        response.setBody(serviceResponse);
        return ResponseEntity.ok(response);
    }

}
