package com.app.foodlane.restaurant.repository;

import com.app.foodlane.restaurant.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("SELECT DISTINCT m FROM MenuItem m " +
            "LEFT JOIN FETCH m.customizationGroups g " +
            "LEFT JOIN FETCH g.customizationOptions o " +
            "WHERE m.menuItemId = :menuItemId")
    Optional<MenuItem> findByIdWithCustomizations(@Param("menuItemId") Long menuItemId);

}
