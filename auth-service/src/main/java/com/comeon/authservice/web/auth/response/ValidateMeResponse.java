package com.comeon.authservice.web.auth.response;

import lombok.Getter;

@Getter
public class ValidateMeResponse {

    private Long userId;

    public ValidateMeResponse(Long userId) {
        this.userId = userId;
    }
}
