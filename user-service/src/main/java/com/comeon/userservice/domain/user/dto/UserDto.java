package com.comeon.userservice.domain.user.dto;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {

    private Long id;
    private String oauthId;
    private OAuthProvider provider;
    private String email;
    private String name;
    private String profileImgUrl;
    private String nickname;
    private Role role;

    @Builder
    public UserDto(Long id, String oauthId, OAuthProvider provider, String email,
                   String name, String profileImgUrl, String nickname, Role role) {
        this.id = id;
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
        this.nickname = nickname;
        this.role = role;
    }
}
