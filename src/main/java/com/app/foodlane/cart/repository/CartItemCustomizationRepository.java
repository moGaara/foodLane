package com.app.foodlane.cart.repository;

import com.app.foodlane.cart.entity.CartItemCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemCustomizationRepository extends JpaRepository<CartItemCustomization,Long> {
}
