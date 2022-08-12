package com.comeon.authservice.auth.jwt.exception;

// 확인 가능한 Jwt Token 이 없을 시 발생
public class JwtNotExistException extends RuntimeException {

    public JwtNotExistException(String message) {
        super(message);
    }

    public JwtNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
