package com.app.foodlane.cart.repository;

import com.app.foodlane.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Modifying
    @Query("""
        DELETE FROM CartItem ci 
        WHERE ci.cartItemId = :cartItemId 
          AND ci.cart.customer.customerId = :customerId
          AND ci.cart.status = :cartStaus
    """)
    int deleteByCartItemIdAndCustomerId(
            @Param("cartItemId") Long cartItemId,
            @Param("customerId") Long customerId,
            @Param("cartStaus") String cartStaus
    );
}
