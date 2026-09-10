package com.app.foodlane.order.repository;

import com.app.foodlane.order.entity.OrderDeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDeliveryAddressRepository extends JpaRepository<OrderDeliveryAddress, Long> {
}
