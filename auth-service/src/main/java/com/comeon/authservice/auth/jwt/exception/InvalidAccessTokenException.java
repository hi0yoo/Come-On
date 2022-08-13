package com.comeon.authservice.auth.jwt.exception;

import io.jsonwebtoken.JwtException;

// 어떤 Jwt 검증시 발생한 예외인지 구분하기 위한 예외
public class InvalidAccessTokenException extends JwtException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }

    public InvalidAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
