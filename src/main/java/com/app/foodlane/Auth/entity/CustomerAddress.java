package com.app.foodlane.Auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "customer_address", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotBlank
    @Size(max = 100)
    @Column(name = "building_name", nullable = false)
    private String buildingName;

    @NotBlank
    @Size(max = 500)
    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @NotBlank
    @Size(max = 30)
    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;
}
