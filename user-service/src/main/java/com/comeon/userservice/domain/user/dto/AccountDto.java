package com.comeon.userservice.domain.user.dto;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AccountDto {

    private Long id;
    private String oauthId;
    private OAuthProvider provider;
    private String email;
    private String name;
    private String profileImgUrl;

    @Builder
    public AccountDto(Long id, String oauthId, OAuthProvider provider,
                      String email, String name, String profileImgUrl) {
        this.id = id;
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

}
