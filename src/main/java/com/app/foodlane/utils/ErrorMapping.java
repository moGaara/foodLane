package com.app.foodlane.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ErrorMapping {
    REQUIRED_CART_ITEM_ID(ErrorConstants.REQUIRED_CART_ITEM_ID_CODE, ErrorConstants.REQUIRED_CART_ITEM_ID_DESC),
    INVALID_CART_ITEM_ID(ErrorConstants.INVALID_CART_ITEM_ID_CODE, ErrorConstants.INVALID_CART_ITEM_ID_DESC),
    SOMETHING_WENT_WRONG(ErrorConstants.DEFAULT_ERROR_CODE, ErrorConstants.DEFAULT_ERROR_DESC),
    SUCCESS(ErrorConstants.SUCCESS_CODE, ErrorConstants.SUCCESS_DESC);

    private final String code;
    private final String desc;

    // Static HashMap for O(1) resolution from error code to ErrorMapping enum
    private static final Map<String, ErrorMapping> CODE_MAP = new HashMap<>();
    static {
        for (ErrorMapping error : values()) {
            CODE_MAP.put(error.getCode(), error);
        }
    }

    // O(1) lookup method
    public static ErrorMapping getErrorByCode(String code) {
        return CODE_MAP.getOrDefault(code, SOMETHING_WENT_WRONG);
    }
}
