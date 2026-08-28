package com.app.foodlane.cart.service;

import com.app.foodlane.cart.entity.CartItemCustomization;
import com.app.foodlane.cart.repository.CartItemCustomizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemCustomizationService {
    private final CartItemCustomizationRepository cartItemCustomizationRepository;

    public CartItemCustomization saveEntity(CartItemCustomization cartItemCustomization){
        return cartItemCustomizationRepository.save(cartItemCustomization);
    }
}
