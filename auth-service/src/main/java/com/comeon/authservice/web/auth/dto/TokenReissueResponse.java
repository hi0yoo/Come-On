package com.comeon.authservice.web.auth.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class TokenReissueResponse {

    private final String accessToken;
    private final Instant expiry;

    public TokenReissueResponse(String accessToken, Instant expiry) {
        this.accessToken = accessToken;
        this.expiry = expiry;
    }
}
