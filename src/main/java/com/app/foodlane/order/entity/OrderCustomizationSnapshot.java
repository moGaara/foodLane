package com.app.foodlane.order.entity;

import java.math.BigDecimal;

/**
 * Immutable record of a customization selected when an order was placed.
 */
public record OrderCustomizationSnapshot(
        Long customizationOptionId,
        String nameSnapshot,
        BigDecimal unitPriceSnapshot,
        Integer quantity
) {
}
