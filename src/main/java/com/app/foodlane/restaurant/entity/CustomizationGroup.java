package com.app.foodlane.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "customization_group", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomizationGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customization_group_id")
    private Long customizationGroupId;

    @NotBlank(message = "Group name is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotNull
    @Builder.Default
    @Column(name = "required", nullable = false)
    private Boolean required = false;

    @NotNull
    @Min(value = 0, message = "Min select must be greater than or equal to 0")
    @Builder.Default
    @Column(name = "min_select", nullable = false)
    private Integer minSelect = 0;

    @NotNull
    @Min(value = 0, message = "Max select must be at least 0")
    @Max(value = 6, message = "Max select cannot exceed 6")
    @Builder.Default
    @Column(name = "max_select", nullable = false)
    private Integer maxSelect = 1;
}