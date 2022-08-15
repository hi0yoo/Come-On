package com.comeon.authservice.auth.jwt.exception;

public class AccessTokenNotExpiredException extends RuntimeException {
    public AccessTokenNotExpiredException(String message) {
        super(message);
    }

    public AccessTokenNotExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
