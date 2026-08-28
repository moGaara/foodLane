package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.request.UpdateCartItemRequest;
import com.app.foodlane.cart.dto.response.CartResponse;
import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.CartService;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.reswrapper.GenericRes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final IHelloService iHelloService;
    private final CartService cartService;

    @GetMapping("/hello")
    public ResponseEntity<GenericRes<Void>> hello() {
        GenericRes<Void> res = new GenericRes<>();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/hello/{name}")
    public ResponseEntity<GenericRes<HelloDtoRes>> helloName(@PathVariable String name,
            @RequestHeader("Authorization") String authorization) {
        long id = CommonFunctions.extractID(authorization);
        HelloDtoRes serviceRes = iHelloService.helloDto(name, id);
        GenericRes<HelloDtoRes> res = new GenericRes<>();
        res.setBody(serviceRes);
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
        CartResponse serviceResponse = cartService.updateCartItem(
                cartId,
                cartItemId,
                customerId,
                request);
        GenericRes<CartResponse> resp = new GenericRes<>();
        resp.setBody(serviceResponse);
        return ResponseEntity.ok(resp);
    }
}
