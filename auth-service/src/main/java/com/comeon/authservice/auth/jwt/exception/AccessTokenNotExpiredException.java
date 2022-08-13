package com.comeon.authservice.auth.jwt.exception;

import io.jsonwebtoken.JwtException;

public class AccessTokenNotExpiredException extends JwtException {
    public AccessTokenNotExpiredException(String message) {
        super(message);
    }

    public AccessTokenNotExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
