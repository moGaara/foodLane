package com.app.foodlane.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddToCartRequestDto(@NotNull Long restaurantId,
                                  @NotNull Long menuItemId,
                                  @Min(1) Integer menuItemQuantity,
                                  String menuItemNote,
                                  List<CustomizationSelectionDto> customizationSelectionDtoList) {
}
