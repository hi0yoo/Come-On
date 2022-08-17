package com.comeon.authservice.auth.exception;

public class RefreshTokenNotExistException extends RuntimeException {

    public RefreshTokenNotExistException() {
        super("Refresh Token이 존재하지 않습니다.");
    }
}
