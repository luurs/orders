package com.lera.orders.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionValidationHandler {

    @ExceptionHandler
    public ResponseEntity<ValidationResponse> handleException(ValidationException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new ValidationResponse(e.getMessage(), e.getStatus()));
    }
}
