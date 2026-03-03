package com.lera.orders.util;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {
    private final String message;
    private final HttpStatus status;

    public ValidationException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
