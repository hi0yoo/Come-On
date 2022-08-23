package com.comeon.meetingservice.common.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {

    EMPTY_FILE(101, BAD_REQUEST, "업로드 파일이 없는 경우 발생합니다."),
    UPLOAD_FAIL(102, INTERNAL_SERVER_ERROR, "파일 업로드에 실패할 경우 발생합니다."),
    VALIDATION_FAIL(103, BAD_REQUEST, "요청 데이터 검증에 실패했을 경우 발생합니다."),
    ENTITY_NOT_FOUND(104, BAD_REQUEST, "해당 식별자를 가진 리소스가 없을 경우 발생합니다."),
    MEETING_USER_NOT_INCLUDE(105, BAD_REQUEST, "해당 모임에 유저가 속해있지 않을 경우 발생합니다."),
    BINDING_RESULT_NOT_FOUND(106, INTERNAL_SERVER_ERROR, "서버 측 오류입니다. 추후에도 해결될 가능성이 없기에 오류 해결 문의 부탁드립니다."),
    INVALID_MEETING_CODE(107, BAD_REQUEST, "유효하지 않은 초대코드일 경우 발생합니다. (없거나, 만료된 경우입니다.)");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(Integer errorCode, HttpStatus httpStatus, String message) {
        this.code = errorCode;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getName() {
        return name();
    }

}
