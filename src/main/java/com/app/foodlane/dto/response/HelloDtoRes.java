package com.app.foodlane.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class HelloDtoRes {
    private long customerID;
    private String customerName;
}
