package com.app.foodlane.cart.dto.response;

import java.math.BigDecimal;

public record CartItemCustomizationResponseDto(Long customizationOptionId,
                                               String optionName,
                                               BigDecimal priceSnapshot,
                                               Integer quantity) {
}
