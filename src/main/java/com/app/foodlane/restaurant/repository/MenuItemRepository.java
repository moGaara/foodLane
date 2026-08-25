package com.app.foodlane.restaurant.repository;

import com.app.foodlane.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {

}
