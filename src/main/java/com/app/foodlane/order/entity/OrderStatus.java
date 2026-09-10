package com.app.foodlane.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "order_status", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String code;

    @Size(max = 255)
    private String description;
}
