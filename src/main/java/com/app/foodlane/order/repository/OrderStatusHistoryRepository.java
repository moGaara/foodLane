package com.app.foodlane.order.repository;

import com.app.foodlane.order.entity.Order;
import com.app.foodlane.order.entity.OrderStatusHistory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByOrderOrderByCreatedAtAsc(
        Order order
    );
}
