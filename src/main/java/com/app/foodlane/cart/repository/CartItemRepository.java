package com.app.foodlane.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.foodlane.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /** Enforces item, cart, customer, and active-status ownership in one query. */
    Optional<CartItem> findByCartItemIdAndCartCartIdAndCartCustomerCustomerIdAndCartStatus(
            Long cartItemId,
            Long cartId,
            Long customerId,
            String status);

    /** Loads the cart items used to build the updated cart response and totals. */
    List<CartItem> findAllByCartCartId(Long cartId);
}
