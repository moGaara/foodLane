package com.app.foodlane.payment.entity;

import com.app.foodlane.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Size(max = 255)
    @Column(name = "transaction_reference")
    private String transactionReference;
}
