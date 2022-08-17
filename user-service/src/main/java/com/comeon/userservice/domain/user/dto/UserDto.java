package com.comeon.userservice.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {

    private Long id;
    private String oAuthId;
    private String email;
    private String name;
    private String nickname;
    private String profileImgUrl;
    private String providerName;
    private String role;

    @Builder
    public UserDto(Long id, String oAuthId, String email, String name, String nickname,
                   String profileImgUrl, String providerName, String role) {
        this.id = id;
        this.oAuthId = oAuthId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.providerName = providerName;
        this.role = role;
    }
}
