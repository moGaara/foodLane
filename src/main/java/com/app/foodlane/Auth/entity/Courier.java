package com.app.foodlane.Auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Delivery profile for a user who can be assigned to an order. */
@Entity
@Table(name = "courier", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {

    @Id
    @Column(name = "courier_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "courier_id")
    private User user;

    @NotBlank
    @Size(max = 50)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Size(max = 30)
    @Column(name = "license_plate", length = 30)
    private String licensePlate;

    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean available = true;
}
