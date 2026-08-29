package com.app.foodlane.cart.dto.request;

import java.util.List;

import com.app.foodlane.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Partial update body: every field is optional, but at least one must be present. */
@Data
public class UpdateCartItemRequest {
    @Min(value = 0, message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    @Max(value = 99, message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    private Integer quantity;

    @Size(max = 1000, message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    private String itemNote;

    @Valid
    private List<CartItemCustomizationRequest> customizations;

    @AssertTrue(message = ErrorConstants.INVALID_CART_UPDATE_CODE)
    public boolean isUpdatePresent() {
        // Checking for null preserves PATCH semantics; an empty string/list is still an update.
        return quantity != null || itemNote != null || customizations != null;
    }
}
