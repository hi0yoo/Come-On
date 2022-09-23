package com.comeon.authservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(600, HttpStatus.INTERNAL_SERVER_ERROR, "죄송합니다. 서버 내부 오류입니다."),
    NO_AUTHORIZATION_HEADER(601, HttpStatus.UNAUTHORIZED, "인증 헤더를 찾을 수 없습니다."),
    INVALID_ACCESS_TOKEN(602, HttpStatus.UNAUTHORIZED, "인증 헤더 검증에 실패하였습니다."),
    NOT_SUPPORTED_TOKEN_TYPE(603, HttpStatus.UNAUTHORIZED, "인증 헤더의 토큰 타입이 유효하지 않습니다."),

    NO_REFRESH_TOKEN(661, HttpStatus.UNAUTHORIZED, "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(662, HttpStatus.UNAUTHORIZED, "리프레시 토큰 검증에 실패하였습니다."),
    NOT_EXPIRED_ACCESS_TOKEN(663, HttpStatus.BAD_REQUEST, "엑세스 토큰이 만료되지 않아 재발급 할 수 없습니다."),

    NO_PARAM_REDIRECT_URI(671, HttpStatus.BAD_REQUEST, "로그아웃 성공시, 리다이렉트 할 URI가 없습니다."),
    NO_PARAM_TOKEN(672, HttpStatus.UNAUTHORIZED, "로그아웃에 사용될 엑세스 토큰이 없습니다."),
    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return name();
    }
}
