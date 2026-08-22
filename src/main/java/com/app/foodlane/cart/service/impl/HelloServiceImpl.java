package com.app.foodlane.cart.service.impl;

import com.app.foodlane.cart.dto.response.HelloDtoRes;
import com.app.foodlane.cart.service.IHelloService;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements IHelloService {
    @Override
    public HelloDtoRes helloDto(String name, long id) {
        return HelloDtoRes.builder()
                .customerName(name)
                .customerID(id)
                .build();
    }
}
