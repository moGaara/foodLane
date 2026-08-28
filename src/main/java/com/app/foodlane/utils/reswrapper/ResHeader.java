package com.app.foodlane.utils.reswrapper;

import com.app.foodlane.utils.ErrorMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ResHeader {
    private String statusCode = ErrorMapping.SUCCESS.getCode();
    private String statusDesc = ErrorMapping.SUCCESS.getDesc();
}
