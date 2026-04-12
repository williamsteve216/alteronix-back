package com.alterronix.alteronixback.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadCredentialsException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String statusCode;

    public BadCredentialsException(String message) {
        super(message);
    }

    public BadCredentialsException(String message, String statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
