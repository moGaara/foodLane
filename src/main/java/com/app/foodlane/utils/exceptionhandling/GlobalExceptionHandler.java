package com.app.foodlane.utils.exceptionhandling;

import com.app.foodlane.utils.ErrorConstants;
import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.reswrapper.ResHeader;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResHeader> handleRequestBodyValidation(
            MethodArgumentNotValidException ex) {
        String errorCode = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse(ErrorMapping.SOMETHING_WENT_WRONG.getCode());
        ErrorMapping error = ErrorMapping.getErrorByCode(errorCode);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResHeader.builder()
                        .statusCode(error.getCode())
                        .statusDesc(error.getDesc())
                        .build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResHeader> handleConstraintViolation(ConstraintViolationException ex) {
        String errorCode = ex.getConstraintViolations().iterator().next().getMessage();
        ErrorMapping error = ErrorMapping.getErrorByCode(errorCode);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResHeader.builder()
                        .statusCode(error.getCode())
                        .statusDesc(error.getDesc())
                        .build());
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ResHeader> handleConstraintViolation(HandlerMethodValidationException ex) {
        String errorCode = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .findFirst()
                .map(MessageSourceResolvable::getDefaultMessage)
                .orElse(ErrorMapping.SOMETHING_WENT_WRONG.getCode());

        ErrorMapping error = ErrorMapping.getErrorByCode(errorCode);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResHeader.builder()
                        .statusCode(error.getCode())
                        .statusDesc(error.getDesc())
                        .build());
    }
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResHeader> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ResHeader.builder()
                        .statusCode(ex.getCode())
                        .statusDesc(ex.getDesc())
                        .build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResHeader> handleUnexpectedExceptions(Exception ex) {
        ErrorMapping error = ErrorMapping.getErrorByCode(ErrorConstants.DEFAULT_ERROR_CODE);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResHeader.builder()
                        .statusCode(error.getCode())
                        .statusDesc(error.getDesc())
                        .build());
    }
}
