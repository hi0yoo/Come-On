package com.comeon.authservice.web.auth.dto;

import lombok.Getter;

@Getter
public class TokenReissueResponseDto {

    private String accessToken;
    private Boolean isRefreshTokenReissued;

    public TokenReissueResponseDto(String accessToken, boolean isRefreshTokenReissued) {
        this.accessToken = accessToken;
        this.isRefreshTokenReissued = isRefreshTokenReissued;
    }
}
