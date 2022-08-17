package com.comeon.authservice.auth.exception;

import io.jsonwebtoken.JwtException;

public class InvalidRefreshTokenException extends JwtException {

    public InvalidRefreshTokenException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
