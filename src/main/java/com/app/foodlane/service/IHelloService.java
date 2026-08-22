package com.app.foodlane.service;

import com.app.foodlane.dto.response.HelloDtoRes;

public interface IHelloService {
    HelloDtoRes helloDto(String name, long id);
}
