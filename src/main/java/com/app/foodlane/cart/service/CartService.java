package com.app.foodlane.cart.service;

import com.app.foodlane.cart.entity.CartItem;

import java.util.List;

public interface CartService {
    public List<CartItem> viewCart(Long cartId,Long CustomerId);

    public void clearCart(Long cartId,Long CustomerId);
}
