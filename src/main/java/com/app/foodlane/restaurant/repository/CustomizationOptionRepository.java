package com.app.foodlane.restaurant.repository;

import com.app.foodlane.restaurant.entity.CustomizationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomizationOptionRepository extends JpaRepository<CustomizationOption,Long>{

}
