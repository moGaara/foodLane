package com.app.foodlane.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.foodlane.cart.entity.CartItemCustomization;

public interface CartItemCustomizationRepository extends JpaRepository<CartItemCustomization, Long> {
    /** Loads saved selections for response mapping and price calculation. */
    List<CartItemCustomization> findAllByCartItemCartItemId(Long cartItemId);

    /** Clears previous selections before a supplied customization list is saved. */
    void deleteAllByCartItemCartItemId(Long cartItemId);
}
