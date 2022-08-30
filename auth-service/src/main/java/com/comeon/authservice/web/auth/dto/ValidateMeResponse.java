package com.comeon.authservice.web.auth.dto;

import lombok.Getter;

@Getter
public class ValidateMeResponse {

    public static final String SUCCESS_MESSAGE = "인증 헤더 검증에 성공하였습니다.";

    private final String message;

    public ValidateMeResponse(String message) {
        this.message = message;
    }

    public ValidateMeResponse() {
        this.message = SUCCESS_MESSAGE;
    }
}
