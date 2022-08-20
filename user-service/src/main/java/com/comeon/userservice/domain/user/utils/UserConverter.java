package com.comeon.userservice.domain.user.utils;

import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.User;

public class UserConverter {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .oauthId(user.getOauthId())
                .provider(user.getProvider())
                .email(user.getEmail())
                .name(user.getName())
                .profileImgUrl(user.getProfileImgUrl())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        return User.builder()
                .oauthId(userDto.getOauthId())
                .provider(userDto.getProvider())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .profileImgUrl(userDto.getProfileImgUrl())
                .build();
    }
}
