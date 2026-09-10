package com.app.foodlane.Auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;
}
