package com.comeon.authservice.auth.jwt.exception;

import io.jsonwebtoken.JwtException;

public class InvalidJwtException extends JwtException {

    public InvalidJwtException(String message) {
        super(message);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
