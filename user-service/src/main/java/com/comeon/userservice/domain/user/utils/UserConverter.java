package com.comeon.userservice.domain.user.utils;

import com.comeon.userservice.domain.user.dto.AccountDto;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.Account;
import com.comeon.userservice.domain.user.entity.User;

public class UserConverter {

    public static UserDto toDto(User user) {
        Account account = user.getAccount();
        return UserDto.builder()
                .id(user.getId())
                .accountDto(
                        AccountDto.builder()
                                .oauthId(account.getOauthId())
                                .provider(account.getProvider())
                                .email(account.getEmail())
                                .name(account.getName())
                                .profileImgUrl(account.getProfileImgUrl())
                                .build()
                )
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        AccountDto accountDto = userDto.getAccountDto();
        return User.builder()
                .account(
                        Account.builder()
                                .oauthId(accountDto.getOauthId())
                                .provider(accountDto.getProvider())
                                .email(accountDto.getEmail())
                                .name(accountDto.getName())
                                .profileImgUrl(accountDto.getProfileImgUrl())
                                .build()
                )
                .build();
    }
}
