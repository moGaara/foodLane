package com.app.foodlane.cart.service.impl;

import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepository;

    public CartItem getById(Long id){
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND , "cartItem not found"
                ));
    }

    public CartItem saveEntity(CartItem cartItem){
        return cartItemRepository.save(cartItem);
    }
}
