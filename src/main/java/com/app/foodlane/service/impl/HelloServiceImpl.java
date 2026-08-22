package com.app.foodlane.service.impl;

import com.app.foodlane.dto.response.HelloDtoRes;
import com.app.foodlane.service.IHelloService;
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
