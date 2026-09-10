package com.app.foodlane.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "order_delivery_address", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_address_id")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    @NotBlank
    @Size(max = 100)
    @Column(name = "building_name", nullable = false)
    private String buildingName;
    @NotBlank
    @Column(name = "street_address", nullable = false)
    private String streetAddress;
    @NotBlank
    @Size(max = 30)
    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;
    @Size(max = 1000)
    @Column(name = "delivery_instructions")
    private String deliveryInstructions;
}
