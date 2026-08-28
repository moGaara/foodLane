package com.app.foodlane.cart.dto.response;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class CartItemResponse {
    private Long cartId;
    private Long cartItemId;
    private Long menuItemId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String itemNote;
    private List<CartItemCustomizationResponse> customizations;
}
