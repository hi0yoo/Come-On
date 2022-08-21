package com.comeon.userservice.web.user.response;

import lombok.Getter;

@Getter
public class UserWithdrawResponse {

    public static final String SUCCESS_MESSAGE = "회원 탈퇴 처리가 완료되었습니다.";

    private String message;

    public UserWithdrawResponse() {
        this.message = SUCCESS_MESSAGE;
    }
}
