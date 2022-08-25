package com.comeon.userservice.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    // TODO 메시지 수정
    SERVER_ERROR(900, INTERNAL_SERVER_ERROR, "죄송합니다. 서버 내부 오류입니다."),
    EMPTY_FILE(901, BAD_REQUEST, "파일이 비어있습니다. 확인해주세요."),
    UPLOAD_FAIL(902, INTERNAL_SERVER_ERROR, "파일 업로드에 실패하였습니다."),
    VALIDATION_FAIL(903, BAD_REQUEST, "요청 데이터 검증에 실패하였습니다."),
    ENTITY_NOT_FOUND(904, BAD_REQUEST, "해당 식별자를 가진 리소스가 없습니다."),
    NO_AUTHORITIES(905, FORBIDDEN, "요청을 수행할 권한이 없습니다."),
    ALREADY_WITHDRAW(906, BAD_REQUEST, "탈퇴 처리된 회원입니다."),
    NO_ACCESS_TOKEN(907, UNAUTHORIZED, "인증된 사용자만이 이용 가능합니다."),
    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(Integer code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
