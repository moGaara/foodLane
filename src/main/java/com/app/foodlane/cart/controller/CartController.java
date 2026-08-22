package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.utils.CommonFunctions;
import com.app.foodlane.utils.reswrapper.GenericRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CartController {
    private final IHelloService iHelloService;

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
}
