package com.app.foodlane.restaurant.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DbCustomizationItem {
    private final long customizationItemId;
    private final BigDecimal customizationItemSnapShot;
    private final int customizationItemMax;
    private final int customizationItemMin;
    private final boolean isRequired;

}
