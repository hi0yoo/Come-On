package com.comeon.authservice.auth.jwt.exception;

// 확인 가능한 RefreshToken 이 없을 시 발생
public class RefreshTokenNotExistException extends JwtNotExistException {

    public RefreshTokenNotExistException(String message) {
        super(message);
    }

    public RefreshTokenNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
