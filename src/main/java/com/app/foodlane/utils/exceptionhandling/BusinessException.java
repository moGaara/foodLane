package com.app.foodlane.utils.exceptionhandling;

import com.app.foodlane.utils.ErrorMapping;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
public class BusinessException extends RuntimeException{
    private final String code;
    private final String desc;

    public BusinessException(ErrorMapping error) {
        super(error.getDesc());
        this.code = error.getCode();
        this.desc = error.getDesc();
    }
}
