package com.comeon.authservice.auth.jwt.exception;

// 확인 가능한 AccessToken 이 없을 시 발생
public class AccessTokenNotExistException extends JwtNotExistException {

    public AccessTokenNotExistException(String message) {
        super(message);
    }

    public AccessTokenNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
