package com.comeon.userservice.web.common.response;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ApiResponseCode {

    SUCCESS("요청이 성공하였습니다."),
    BAD_PARAMETER("요청 파라미터가 잘못되었습니다."),
    NOT_FOUND("리소스를 찾지 못했습니다."),
    UNAUTHORIZED("인증에 실패하였습니다."),
    FORBIDDEN("권한이 없습니다."),
    SERVER_ERROR("서버 에러입니다."),
    ;

    private final String message;

    public String getId() {
        return name();
    }

    public String getText() {
        return message;
    }

    public static ApiResponseCode getResponseCode(HttpStatus httpStatus) {
        ApiResponseCode returnCode;
        switch (httpStatus) {
            case CREATED:
                returnCode = SUCCESS;
                break;
            case INTERNAL_SERVER_ERROR:
                returnCode = SERVER_ERROR;
                break;
            case FORBIDDEN:
                returnCode = FORBIDDEN;
                break;
            case NOT_FOUND:
                returnCode = NOT_FOUND;
                break;
            default:
                returnCode = BAD_PARAMETER;
        }
        return returnCode;
    }
}
