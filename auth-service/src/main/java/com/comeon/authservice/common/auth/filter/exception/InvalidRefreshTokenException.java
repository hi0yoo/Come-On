package com.comeon.authservice.common.auth.filter.exception;

import io.jsonwebtoken.JwtException;

public class InvalidRefreshTokenException extends JwtException {

    public InvalidRefreshTokenException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
