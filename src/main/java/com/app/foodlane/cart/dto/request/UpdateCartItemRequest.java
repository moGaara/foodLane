package com.app.foodlane.cart.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Partial update body: every field is optional, but at least one must be present. */
@Data
public class UpdateCartItemRequest {
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 99, message = "Quantity cannot exceed 99")
    private Integer quantity;

    @Size(max = 1000, message = "Item note cannot exceed 1000 characters")
    private String itemNote;

    @Valid
    private List<CartItemCustomizationRequest> customizations;

    @AssertTrue(message = "At least one editable field is required")
    public boolean isUpdatePresent() {
        // Checking for null preserves PATCH semantics; an empty string/list is still an update.
        return quantity != null || itemNote != null || customizations != null;
    }
}
