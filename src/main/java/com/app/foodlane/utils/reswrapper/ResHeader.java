package com.app.foodlane.utils.reswrapper;

import com.app.foodlane.utils.ErrorMapping;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResHeader {
    private String statusCode = ErrorMapping.SUCCESS.getCode();
    private String statusDesc = ErrorMapping.SUCCESS.getDesc();
}
