package com.app.foodlane.Auth.repository;

import com.app.foodlane.Auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}