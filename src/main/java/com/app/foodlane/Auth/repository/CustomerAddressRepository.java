package com.app.foodlane.Auth.repository;

import com.app.foodlane.Auth.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    Optional<CustomerAddress> findByAddressIdAndCustomerCustomerId(Long addressId, Long customerId);
}
