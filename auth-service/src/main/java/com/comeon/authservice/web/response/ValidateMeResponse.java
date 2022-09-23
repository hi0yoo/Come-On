package com.comeon.authservice.web.response;

import lombok.Getter;

@Getter
public class ValidateMeResponse {

    private Long userId;

    public ValidateMeResponse(Long userId) {
        this.userId = userId;
    }
}
