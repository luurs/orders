package com.lera.orders.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionValidationHandler {

    @ExceptionHandler
    public ResponseEntity<ApiResponse> handleException(ApiException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ApiResponse(e.getMessage(), e.getStatus()));
    }
}
