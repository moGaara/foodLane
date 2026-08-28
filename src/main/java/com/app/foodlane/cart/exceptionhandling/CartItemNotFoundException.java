package com.app.foodlane.cart.exceptionhandling;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long cartItemId) {
        super("Cart item not found: " + cartItemId);
    }
}
