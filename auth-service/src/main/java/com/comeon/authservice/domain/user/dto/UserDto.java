package com.comeon.authservice.domain.user.dto;

import com.comeon.authservice.domain.user.entity.OAuthProvider;
import lombok.Getter;

@Getter
public class UserDto {

    private OAuthProvider provider;
    private String oauthId;
    private String email;
    private String name;
    private String profileImgUrl;

    public UserDto(String providerName, String oauthId,
                   String email, String name, String profileImgUrl) {
        this.provider = OAuthProvider.valueOf(providerName.toUpperCase());
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }
}
