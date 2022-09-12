package com.comeon.meetingservice.common.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCode {

    EMPTY_FILE(101, BAD_REQUEST, "업로드 파일이 없는 경우 발생합니다."),
    UPLOAD_FAIL(102, INTERNAL_SERVER_ERROR, "파일 업로드에 실패할 경우 발생합니다. 다시 시도하더라도 오류가 발생한다면 문의 부탁드립니다."),
    VALIDATION_FAIL(103, BAD_REQUEST, "요청 데이터 검증에 실패했을 경우 발생합니다."),
    ENTITY_NOT_FOUND(104, NOT_FOUND, "해당 식별자를 가진 리소스가 없을 경우 발생합니다."),
    MEETING_USER_NOT_INCLUDE(105, FORBIDDEN, "해당 모임에 유저가 속해있지 않을 경우 발생합니다."),
    BINDING_RESULT_NOT_FOUND(106, INTERNAL_SERVER_ERROR, "서버 측 오류입니다. 추후에도 해결될 가능성이 없기에 오류 해결 문의 부탁드립니다."),
    NONEXISTENT_CODE(107, BAD_REQUEST, "해당 초대코드를 가진 모임이 없는 경우 발생합니다."),
    EXPIRED_CODE(108, BAD_REQUEST, "해당 초대코드가 만료된 경우 발생합니다."),
    USER_ALREADY_PARTICIPATE(109, BAD_REQUEST, "이미 모임에 가입한 회원인 경우 발생합니다."),
    USER_ALREADY_SELECT(110, BAD_REQUEST, "이미 해당 날짜를 선택한 회원인 경우 발생합니다."),
    PERIOD_NOT_EXIST(111, INTERNAL_SERVER_ERROR, "서버 측 오류입니다. 추후에도 해결될 가능성이 없기에 오류 해결 문의 부탁드립니다."),
    INVALID_PERIOD(112, BAD_REQUEST, "기간(시작일과 종료일이 있는 날짜)이 잘못된 경우 발생합니다. 시작일보다 종료일이 이른 경우가 대표적입니다."),
    DATE_NOT_WITHIN_PERIOD(113, BAD_REQUEST, "날짜가 기간(모임의 기간)내에 포함되지 않을 경우 발생합니다."),
    HTTP_MESSAGE_NOT_READABLE(114, BAD_REQUEST, "요청 데이터가 없거나, 데이터의 형식이 잘못되었을 경우에 발생합니다."),
    USER_NOT_SELECT_DATE(115, BAD_REQUEST, "해당 회원이 해당 날짜를 선택하지 않았습니다."),
    MEETING_USER_NOT_HOST(116, FORBIDDEN, "해당 회원이 해당 모임의 주인이 아니기에 권한이 없을 경우 발생합니다."),
    UNEXPIRED_CODE(117, BAD_REQUEST, "코드를 갱신할 때, 해당 초대코드가 아직 만료되지 않았을 경우 발생합니다."),
    MODIFY_HOST_NOT_SUPPORT(118, BAD_REQUEST, "유저 권한을 수정할 때 HOST로 변경 시에 발생합니다. (HOST 변경은 막혀있습니다.)"),
    MODIFY_HOST_IMPOSSIBLE(119, BAD_REQUEST, "HOST 권한을 가진 유저의 권한을 수정하려고 할 경우 발생합니다. (HOST는 권한 수정 불가능입니다.)"),
    AUTHORIZATION_FAIL(120, FORBIDDEN, "요청 회원이 해당 요청을 처리할 권한이 없을 경우 발생합니다."),
    AUTHORIZATION_UNABLE(121, INTERNAL_SERVER_ERROR, "서버 측 오류입니다. 추후에도 해결될 가능성이 없기에 오류 해결 문의 부탁드립니다.");

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
