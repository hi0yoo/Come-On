package com.comeon.userservice.domain.user.utils;

import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.User;

public class UserConverter {

    public static User toUserRoleEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .oauthId(userDto.getOauthId())
                .provider(userDto.getProvider())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .profileImgUrl(userDto.getProfileImgUrl())
                .build();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .oauthId(user.getOauthId())
                .provider(user.getProvider())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .role(user.getRole())
                .build();
    }
}
