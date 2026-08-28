package com.app.foodlane.Customer.repository;

import com.app.foodlane.Auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerReposiory extends JpaRepository<Customer,Long> {
    @Override
    Optional<Customer> findById(Long customerId);
}
