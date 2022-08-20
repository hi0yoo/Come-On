package com.comeon.userservice.web.common.response;

public enum ErrorCode {

    INTERNAL_SERVER_ERROR(600, "서버 내부 오류"),
    VALIDATE_ERROR(601, "요청 파라미터 검증 오류"),
    ENTITY_NOT_FOUND(602, "해당 식별자를 가진 리소스가 없을 경우 발생"),
    ;

    private final Integer code;
    private final String description;

    ErrorCode(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
