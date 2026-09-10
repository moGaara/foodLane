package com.app.foodlane.order.repository;

import com.app.foodlane.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {
    Optional<OrderStatus> findByCode(String code);

    boolean existsByCode(String code);
}
