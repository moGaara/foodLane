package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.service.IDeleteCartItemService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.ErrorConstants;
import com.app.foodlane.utils.reswrapper.GenericRes;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final IDeleteCartItemService iDeleteCartItemService;

    @DeleteMapping("/cart-item/{cartItemId}")
    public ResponseEntity<GenericRes<Void>> removeCartItem(@PathVariable @Positive(message = ErrorConstants.INVALID_CART_ITEM_ID_CODE) long cartItemId,
                                                             @RequestHeader("Authorization") String authorization){

        long customerId = CommonFunctions.extractID(authorization);
        iDeleteCartItemService.deleteItem(customerId, cartItemId);
        GenericRes<Void> res = new GenericRes<>();
        return ResponseEntity.ok(res);
    }
}
