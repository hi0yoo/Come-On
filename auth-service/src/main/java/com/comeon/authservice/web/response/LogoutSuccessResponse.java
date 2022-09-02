package com.comeon.authservice.web.response;

import lombok.Getter;

@Getter
public class LogoutSuccessResponse {
    private String message;

    public LogoutSuccessResponse(String message) {
        this.message = message;
    }
}
