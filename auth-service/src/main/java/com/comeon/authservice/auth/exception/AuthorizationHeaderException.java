package com.comeon.authservice.auth.exception;

public class AuthorizationHeaderException extends RuntimeException {

    public AuthorizationHeaderException() {
        super("인증 헤더에 AccessToken을 찾을 수 없습니다.");
    }
}
