package com.app.foodlane.cart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant", schema = "FoodLand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @NotBlank(message = "Restaurant name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull(message = "Open status is required")
    @Builder.Default
    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}