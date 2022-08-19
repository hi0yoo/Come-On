package com.comeon.authservice.common.auth.filter.exception;

public class RefreshTokenNotExistException extends RuntimeException {

    public RefreshTokenNotExistException() {
        super("Refresh Token이 존재하지 않습니다.");
    }
}
