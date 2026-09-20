package com.app.foodlane.order.repository;

import com.app.foodlane.order.entity.Order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndCustomerCustomerId(
            Long orderId,
            Long customerId);
}
