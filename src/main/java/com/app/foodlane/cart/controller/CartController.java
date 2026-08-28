package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.response.CartItemDto;
import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.entity.CartItem;
import com.app.foodlane.cart.service.CartService;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.reswrapper.GenericRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
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



    @GetMapping("/cart/{cartId}")
    public ResponseEntity<GenericRes<List<CartItemDto>>> viewCart(@PathVariable Long cartId,  @RequestHeader("Authorization") String authorization ){

        long customerId = CommonFunctions.extractID(authorization);

        List<CartItem> cartItemList = cartService.viewCart(cartId, customerId);

        List<CartItemDto> cartItemDtos =  cartItemList.stream()
                .map(cartItem -> buildCartItemDto(cartItem, cartId))
                .toList();
        GenericRes<List<CartItemDto>> res = new GenericRes<>();
        res.setBody(cartItemDtos);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/cart/{cartId}")
    public ResponseEntity<GenericRes<Void>> clearCart(
            @PathVariable Long cartId,
            @RequestHeader("Authorization") String authorization) {
        long customerId = CommonFunctions.extractID(authorization);
        cartService.clearCart(cartId, customerId);

        GenericRes<Void> response = new GenericRes<>();

        return ResponseEntity.ok(response);
    }

    private CartItemDto buildCartItemDto(CartItem cartItem, Long  cartId) {
        return CartItemDto.builder()
                .cartItemId(cartItem.getCartItemId())
                .cartId(cartId)
                .quantity(cartItem.getQuantity())
                .unitPriceSnapshot(cartItem.getUnitPriceSnapshot())
                .itemNote(cartItem.getItemNote())
                .build();
    }

}
