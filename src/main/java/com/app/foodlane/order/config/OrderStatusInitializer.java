package com.app.foodlane.order.config;

import com.app.foodlane.order.entity.OrderStatus;
import com.app.foodlane.order.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ensures the order-state reference data required to place an order exists. */
@Component
@RequiredArgsConstructor
public class OrderStatusInitializer implements ApplicationRunner {

    private final OrderStatusRepository orderStatusRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<OrderStatus> defaults = List.of(
                status("PENDING", "Order was placed and awaits restaurant confirmation"),
                status("ACCEPTED", "Order was accepted by the restaurant"),
                status("CANCELLED", "Order was cancelled")
        );

        defaults.stream()
                .filter(status -> !orderStatusRepository.existsByCode(status.getCode()))
                .forEach(orderStatusRepository::save);
    }

    private OrderStatus status(String code, String description) {
        return OrderStatus.builder()
                .code(code)
                .description(description)
                .build();
    }
}
