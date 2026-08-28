package com.app.foodlane.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.foodlane.restaurant.entity.CustomizationOption;

public interface CustomizationOptionRepository extends JpaRepository<CustomizationOption, Long> {
}
