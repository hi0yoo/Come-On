package com.comeon.authservice.web.auth.response;

import lombok.Getter;

@Getter
public class UnlinkResponse {

    public static final String SUCCESS_MESSAGE = "유저 연결 끊기 및 로그아웃 처리가 완료되었습니다.";

    private String message;

    public UnlinkResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public UnlinkResponse(String message) {
        this.message = message;
    }
}
