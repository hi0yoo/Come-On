package com.comeon.userservice.web.user.response;

import lombok.Getter;

@Getter
public class UserModifyResponse {

    public static final String SUCCESS_MESSAGE = "회원 정보 수정이 완료되었습니다.";

    private String message;

    public UserModifyResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public UserModifyResponse(String message) {
        this.message = message;
    }
}
