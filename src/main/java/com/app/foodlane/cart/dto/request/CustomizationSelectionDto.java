package com.app.foodlane.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CustomizationSelectionDto(@NotNull Long customizationOptionId,
                                        @Min(1) Integer quantity) {
}
