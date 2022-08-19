package com.comeon.authservice.common.auth.filter.exception;

public class AccessTokenNotExpiredException extends RuntimeException {

    public AccessTokenNotExpiredException() {
        super("Access Token이 만료되지 않아 재발급 할 수 없습니다.");
    }
}
