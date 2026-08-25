package com.app.foodlane.cart.controller;

import com.app.foodlane.Auth.entity.Customer;
import com.app.foodlane.cart.dto.request.AddToCartRequesttDto;
import com.app.foodlane.cart.dto.response.CartResponseDto;
import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.cart.service.impl.CartService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.reswrapper.GenericRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {
    private final IHelloService iHelloService;
    private final CartService cartService;

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

    @PostMapping("/items")
    public ResponseEntity<GenericRes<CartResponseDto>> addItemToCart
            (@AuthenticationPrincipal Customer customer , @RequestBody AddToCartRequesttDto requestDto){
        long id = customer.getCustomerId();
        CartResponseDto responseDto = cartService.addItem(id , requestDto);
        GenericRes<CartResponseDto> res = new GenericRes<>();
        res.setBody(responseDto);
        return ResponseEntity.ok(res);
    }









}
