package com.app.foodlane.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMapping {
    SUCCESS("I000000", "Success"),
    CART_NOT_FOUND("E000001", "Cart not found");

    private final String code;
    private final String desc;
}
