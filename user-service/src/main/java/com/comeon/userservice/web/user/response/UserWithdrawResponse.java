package com.comeon.userservice.web.user.response;

import lombok.Getter;

@Getter
public class UserWithdrawResponse {

    private String message;

    public UserWithdrawResponse(String message) {
        this.message = message;
    }
}
