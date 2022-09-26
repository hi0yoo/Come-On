package com.comeon.authservice.web.response;

import lombok.Getter;

@Getter
public class TokenReissueResponse {

    private final String accessToken;
    private final Long expiry;
    private final Long userId;

    public TokenReissueResponse(String accessToken, Long expiry, Long userId) {
        this.accessToken = accessToken;
        this.expiry = expiry;
        this.userId = userId;
    }
}
