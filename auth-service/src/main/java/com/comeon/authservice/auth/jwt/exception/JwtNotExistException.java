package com.comeon.authservice.auth.jwt.exception;

import io.jsonwebtoken.JwtException;

// 확인 가능한 Jwt Token 이 없을 시 발생
public class JwtNotExistException extends JwtException {

    public JwtNotExistException(String message) {
        super(message);
    }

    public JwtNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
