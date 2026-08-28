package com.app.foodlane.cart.service;

import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;

public interface CartService {

    /** Partially updates an existing cart item and returns the recalculated cart. */
    CartResponse updateCartItem(
            Long cartId,
            Long cartItemId,
            Long customerId,
            UpdateCartItemRequest request);
}
