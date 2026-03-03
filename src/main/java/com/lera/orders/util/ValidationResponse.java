package com.lera.orders.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationResponse {
    private final String message;
    private final HttpStatus status;

    public ValidationResponse(String message, HttpStatus status) {
        super();
        this.message = message;
        this.status = status;
    }
}
