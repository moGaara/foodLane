package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.IDeleteCartItemService;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.ErrorConstants;
import com.app.foodlane.utils.reswrapper.GenericRes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final IHelloService iHelloService;
    private final IDeleteCartItemService iDeleteCartItemService;

    @GetMapping("/hello")
    public ResponseEntity<GenericRes<Void>> hello(){
        GenericRes<Void> res = new GenericRes<>();
        return ResponseEntity.ok(res);
    }
    @GetMapping("/hello/{name}")
    public ResponseEntity<GenericRes<HelloDtoRes>> helloName(@PathVariable String name,
                                                             @RequestHeader("Authorization") String authorization){
        long id = CommonFunctions.extractID(authorization);
        HelloDtoRes serviceRes= iHelloService.helloDto(name, id);
        GenericRes<HelloDtoRes> res = new GenericRes<>();
        res.setBody(serviceRes);
        return ResponseEntity.ok(res);
    }
    @DeleteMapping("/cart-item/{cartItemId}")
    public ResponseEntity<GenericRes<Void>> removeCartItem(@PathVariable @Positive(message = ErrorConstants.INVALID_CART_ITEM_ID_CODE) long cartItemId,
                                                             @RequestHeader("Authorization") String authorization){

        long customerId = CommonFunctions.extractID(authorization);
        iDeleteCartItemService.deleteItem(customerId, cartItemId);
        GenericRes<Void> res = new GenericRes<>();
        return ResponseEntity.ok(res);
    }
}
