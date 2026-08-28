package com.app.foodlane.cart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("""
            DELETE FROM CartItem ci
            WHERE ci.cartItemId = :cartItemId
              AND ci.cart.customer.customerId = :customerId
              AND ci.cart.status = :cartStatus
            """)
    int deleteByCartItemIdAndCustomerId(
            @Param("cartItemId") Long cartItemId,
            @Param("customerId") Long customerId,
            @Param("cartStatus") String cartStatus);
}
