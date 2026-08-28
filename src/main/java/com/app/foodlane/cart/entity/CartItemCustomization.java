package com.app.foodlane.cart.entity;

import com.app.foodlane.restaurant.entity.CustomizationOption;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_item_customization", schema = "foodland", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cart_item_option", columnNames = {"cart_item_id", "customization_option_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemCustomization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_customization_id")
    private Long cartItemCustomizationId;

    @NotNull(message = "Cart item reference is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItem cartItem;

    @NotNull(message = "Customization option reference is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customization_option_id", nullable = false)
    private CustomizationOption customizationOption;

    @NotNull
    @PositiveOrZero(message = "Price snapshot must be positive or zero")
    @Builder.Default
    @Column(name = "price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSnapshot = BigDecimal.ZERO;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
}
