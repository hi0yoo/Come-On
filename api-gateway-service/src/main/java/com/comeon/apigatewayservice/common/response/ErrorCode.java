package com.comeon.apigatewayservice.common.response;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UnauthorizedException("101", "Authorization Header가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    NoPermissionException("102", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public static ErrorCode createErrorCode(Throwable throwable) {
        return ErrorCode.valueOf(throwable.getClass().getSimpleName());
    }
}
