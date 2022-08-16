package com.comeon.apigatewayservice.common.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Authorization Header 검증에 실패하였습니다.");
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
