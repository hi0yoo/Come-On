package com.comeon.authservice.domain.user.dto;

import lombok.Getter;

@Getter
public class UserDto {

    private String providerName;
    private String oauthId;
    private String email;
    private String name;
    private String profileImgUrl;

    public UserDto(String providerName, String oauthId,
                   String email, String name, String profileImgUrl) {
        this.providerName = providerName;
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }
}
