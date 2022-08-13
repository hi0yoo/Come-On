package com.comeon.authservice.web.auth.dto;

import lombok.Getter;

@Getter
public class TokenReissueResponse {

    private final String accessToken;

    public TokenReissueResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
