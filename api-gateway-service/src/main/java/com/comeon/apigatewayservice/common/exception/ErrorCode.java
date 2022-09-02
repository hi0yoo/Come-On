package com.comeon.apigatewayservice.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {

    SERVER_ERROR(600, INTERNAL_SERVER_ERROR, "죄송합니다. 서버 내부 오류입니다."),
    NO_AUTHORIZATION_HEADER(601, UNAUTHORIZED, "인증 헤더를 찾을 수 없습니다."),
    INVALID_ACCESS_TOKEN(602, UNAUTHORIZED, "인증 헤더 검증에 실패하였습니다."),
    NO_PERMISSION(603, FORBIDDEN, "요청을 수행할 권한이 없습니다."),

    // Matching ResponseStatusException
    BAD_REQUEST(611, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_FOUND(612, HttpStatus.NOT_FOUND, "허용되지 않은 경로입니다."),
    METHOD_NOT_ALLOWED(613, HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 Http Method 입니다.")
    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    // for Matching ResponseStatusException
    public static ErrorCode byResponseStatusExceptionStatus(HttpStatus httpStatus) {
        ErrorCode errorCode;
        switch (httpStatus) {
            case NOT_FOUND:
                errorCode = NOT_FOUND;
                break;
            case METHOD_NOT_ALLOWED:
                errorCode = METHOD_NOT_ALLOWED;
                break;
            default:
                errorCode = BAD_REQUEST;
        }
        return errorCode;
    }
}
