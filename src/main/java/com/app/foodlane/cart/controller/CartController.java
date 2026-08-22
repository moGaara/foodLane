package com.app.foodlane.cart.controller;

import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.IHelloService;
import com.app.foodlane.utils.reswrapper.GenericRes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<GenericRes<HelloDtoRes>> helloName(@PathVariable String name){
        HelloDtoRes serviceRes= iHelloService.helloDto(name);
        GenericRes<HelloDtoRes> res = new GenericRes<>();
        res.setBody(serviceRes);
        return ResponseEntity.ok(res);
    }
}
