package com.app.foodlane.cart.exceptionhandling;

import com.app.foodlane.utils.ErrorMapping;
import com.app.foodlane.utils.reswrapper.GenericRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<GenericRes<Void>> handleCartNotFound(
            CartNotFoundException ex) {

        GenericRes<Void> response = new GenericRes<>();
        response.getHeader().setStatusCode(
                ErrorMapping.CART_NOT_FOUND.getCode()
        );
        response.getHeader().setStatusDesc(ex.getMessage());


        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
