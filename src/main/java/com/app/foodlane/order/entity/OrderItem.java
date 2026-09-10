package com.app.foodlane.order.entity;

import com.app.foodlane.restaurant.entity.MenuItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "order_item", schema = "foodland")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @NotBlank
    @Size(max = 150)
    @Column(name = "item_name_snapshot", nullable = false)
    private String itemNameSnapshot;

    @NotNull
    @PositiveOrZero
    @Column(name = "unit_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @NotNull
    @Min(1)
    @Max(99)
    @Column(name = "selected", nullable = false)
    private Integer quantity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "customizations_snapshot", columnDefinition = "jsonb")
    private List<OrderCustomizationSnapshot> customizationsSnapshot;

    @Size(max = 500)
    @Column(name = "item_note")
    private String itemNote;
}
