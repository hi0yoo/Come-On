package com.comeon.authservice.web.common.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public enum ErrorCode {

    // TODO 예외 처리... 문제 있음...
    AccessTokenNotExpiredException("701", "만료되지 않은 Access Token은 재발급 할 수 없습니다.", HttpStatus.BAD_REQUEST),
    JwtNotExistException("702", "Jwt가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),
    InvalidAccessTokenException("703", "유효하지 않은 Access Token 입니다.", HttpStatus.UNAUTHORIZED),
    JwtException("704", "유효하지 않은 Refresh Token 입니다.", HttpStatus.UNAUTHORIZED),
    ExpiredJwtException("705", "Refresh Token이 만료 되었습니다.", HttpStatus.UNAUTHORIZED),
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
