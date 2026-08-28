package com.app.foodlane.cart.service;

import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;

public interface UpdateCartService {
    CartResponse updateCartItem(
            Long cartId,
            Long cartItemId,
            Long customerId,
            UpdateCartItemRequest request);
}
