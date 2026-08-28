package com.app.foodlane.cart.repo;

import com.app.foodlane.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByCart_Id(Long cartId);
}
