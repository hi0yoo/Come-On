package com.comeon.userservice.domain.user.utils;

import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;

public class UserConverter {

    public static User toUserRoleEntity(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .oAuthId(userDto.getOAuthId())
                .provider(OAuthProvider.valueOf(userDto.getProviderName()))
                .email(userDto.getEmail())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .profileImgUrl(userDto.getProfileImgUrl())
                .build();
    }

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .oAuthId(user.getOAuthId())
                .providerName(user.getProvider().name())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .role(user.getRole().getRoleValue())
                .build();
    }
}
