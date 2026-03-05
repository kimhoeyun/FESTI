package com.example.FESTI.application.auth;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(String message) {
        this(HttpStatus.UNAUTHORIZED, message);
    }

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AuthException(String message, Throwable cause) {
        this(HttpStatus.UNAUTHORIZED, message, cause);
    }

    public AuthException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
