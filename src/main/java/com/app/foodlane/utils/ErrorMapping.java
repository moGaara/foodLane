package com.app.foodlane.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMapping {
    SUCCESS("I000000", "Success");

    private final String code;
    private final String desc;
}
