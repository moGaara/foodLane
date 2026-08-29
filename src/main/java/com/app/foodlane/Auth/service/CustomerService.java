package com.app.foodlane.Auth.service;

import com.app.foodlane.Auth.entity.Customer;
import com.app.foodlane.Auth.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer getById(Long id){
        return customerRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"customer not found"));
    }
}
