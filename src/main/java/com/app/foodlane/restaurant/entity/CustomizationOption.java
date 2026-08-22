package com.app.foodlane.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "customization_option", schema = "FoodLand", uniqueConstraints = {
        @UniqueConstraint(name = "uq_option_group_name", columnNames = {"customization_group_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomizationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customization_option_id")
    private Long customizationOptionId;

    @NotNull(message = "Customization group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_group_id", nullable = false)
    private CustomizationGroup customizationGroup;

    @NotBlank(message = "Option name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @PositiveOrZero(message = "Option price must be positive or zero")
    @Builder.Default
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
}