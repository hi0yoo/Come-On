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
    NONEXISTENT_CODE(107, BAD_REQUEST, "해당 초대코드를 가진 모임이 없는 경우 발생합니다."),
    EXPIRED_CODE(108, BAD_REQUEST, "해당 초대코드가 만료된 경우 발생합니다."),
    USER_ALREADY_PARTICIPATE(109, BAD_REQUEST, "이미 모임에 가입한 회원인 경우 발생합니다."),
    USER_ALREADY_SELECT(110, BAD_REQUEST, "이미 해당 날짜를 선택한 회원인 경우 발생합니다."),
    PERIOD_NOT_EXIST(111, INTERNAL_SERVER_ERROR, "서버 측 오류입니다. 추후에도 해결될 가능성이 없기에 오류 해결 문의 부탁드립니다."),
    INVALID_PERIOD(112, BAD_REQUEST, "기간(시작일과 종료일이 있는 날짜)이 잘못된 경우 발생합니다. 시작일보다 종료일이 이른 경우가 대표적입니다."),
    DATE_NOT_WITHIN_PERIOD(113, BAD_REQUEST, "날짜가 기간(모임의 기간)내에 포함되지 않을 경우 발생합니다.");

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
