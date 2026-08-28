package com.app.foodlane.cart.repo;

import com.app.foodlane.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findyByCart_Id(Long cartId);

    void deleteByCart_Id(Long cartId);

}
