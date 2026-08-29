package com.app.foodlane.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartItemResponseDto(Long cartItemId,
                                  Long menuItemId,
                                  String menuItemName,
                                  BigDecimal unitPrice,
                                  Integer quantity,
                                  String itemNote,
                                  BigDecimal itemTotalPrice,
                                  List<CartItemCustomizationResponseDto> customizations) {
}
