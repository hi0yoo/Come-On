package com.comeon.userservice.domain.user.dto;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {

    private Long id;
    private String oauthId;
    private String email;
    private String name;
    private String nickname;
    private String profileImgUrl;
    private OAuthProvider provider;
    private Role role;

    @Builder
    public UserDto(Long id, String oauthId, String email, String name, String nickname,
                   String profileImgUrl, OAuthProvider provider, Role role) {
        this.id = id;
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.provider = provider;
        this.role = role;
    }
}
