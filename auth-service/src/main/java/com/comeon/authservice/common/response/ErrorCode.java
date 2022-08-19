package com.comeon.authservice.common.response;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(700, "서버 내부 에러입니다."),
    NOT_EXIST_AUTHORIZATION_HEADER(701, "인증 헤더를 찾을 수 없습니다."),
    NOT_EXIST_REFRESH_TOKEN(702, "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_ACCESS_TOKEN(703, "Access Token이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(704, "Refresh Token이 유효하지 않습니다."),
    NOT_EXPIRED_ACCESS_TOKEN(705, "Access Token이 만료되지 않아 재발급 할 수 없습니다."),
    ;

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return name();
    }
}
