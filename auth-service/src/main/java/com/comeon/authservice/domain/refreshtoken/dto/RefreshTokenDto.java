package com.comeon.authservice.domain.refreshtoken.dto;

import com.comeon.authservice.domain.user.entity.User;
import lombok.Getter;

@Getter
public class RefreshTokenDto {

    private User user;
    private String refreshToken;

    public RefreshTokenDto(User user, String refreshToken) {
        this.user = user;
        this.refreshToken = refreshToken;
    }
}
