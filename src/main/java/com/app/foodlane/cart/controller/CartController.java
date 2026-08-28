package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.request.AddToCartRequestDto;
import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.service.CartService;
import com.app.foodlane.cart.service.IDeleteCartItemService;
import com.app.foodlane.cart.service.UpdateCartService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.ErrorConstants;
import com.app.foodlane.utils.reswrapper.GenericRes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final IDeleteCartItemService deleteCartItemService;
    private final CartService addCartService;
    private final UpdateCartService updateCartService;

    @DeleteMapping("/cart/cart-item/{cartItemId}")
    public ResponseEntity<GenericRes<Void>> removeCartItem(
            @PathVariable @Positive(message = ErrorConstants.INVALID_CART_ITEM_ID_CODE)
            long cartItemId,
            @RequestHeader("Authorization") String authorization) {
        long customerId = CommonFunctions.extractID(authorization);
        deleteCartItemService.deleteItem(customerId, cartItemId);
        GenericRes<Void> res = new GenericRes<>();
        return ResponseEntity.ok(res);
    }

    /**
     * Partially updates an existing item in a customer's active cart.
     * Omitted fields remain unchanged, while quantity zero removes the item.
     */
    @PatchMapping("/carts/{cartId}/items/{cartItemId}")
    public ResponseEntity<GenericRes<CartResponse>> updateCartItem(
            @PathVariable Long cartId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader("Authorization") String auth) {
        Long customerId = CommonFunctions.extractID(auth);
        log.info("Received cart-item update: cartId={}, cartItemId={}, customerId={}",
                cartId, cartItemId, customerId);
        CartResponse serviceResponse = updateCartService.updateCartItem(
                cartId,
                cartItemId,
                customerId,
                request);
        GenericRes<CartResponse> resp = new GenericRes<>();
        resp.setBody(serviceResponse);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cart/items")
    public ResponseEntity<GenericRes<CartResponseDto>> addItemToCart(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AddToCartRequestDto request) {
        long customerId = CommonFunctions.extractID(authorization);
        CartResponseDto serviceResponse = addCartService.addItem(customerId, request);
        GenericRes<CartResponseDto> response = new GenericRes<>();
        response.setBody(serviceResponse);
        return ResponseEntity.ok(response);
    }
}
