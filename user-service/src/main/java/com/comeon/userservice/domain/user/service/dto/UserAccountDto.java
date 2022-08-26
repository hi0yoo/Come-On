package com.comeon.userservice.domain.user.service.dto;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.UserAccount;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserAccountDto {

    private Long id;
    private String oauthId;
    private OAuthProvider provider;
    private String email;
    private String name;
    private String profileImgUrl;

    @Builder
    public UserAccountDto(Long id, String oauthId, OAuthProvider provider,
                          String email, String name, String profileImgUrl) {
        this.id = id;
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

    public UserAccount toEntity() {
        return UserAccount.builder()
                .oauthId(oauthId)
                .provider(provider)
                .email(email)
                .name(name)
                .profileImgUrl(profileImgUrl)
                .build();
    }
}
