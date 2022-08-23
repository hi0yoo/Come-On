package com.comeon.userservice.domain.user.utils;

import com.comeon.userservice.domain.user.dto.AccountDto;
import com.comeon.userservice.domain.user.dto.ProfileImgDto;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.Account;
import com.comeon.userservice.domain.user.entity.ProfileImg;
import com.comeon.userservice.domain.user.entity.User;

public class UserConverter {

    public static UserDto toDto(User user) {
        AccountDto accountDto = getAccountDto(user);
        ProfileImgDto profileImgDto = getProfileImgDto(user);

        return UserDto.builder()
                .id(user.getId())
                .accountDto(accountDto)
                .profileImgDto(profileImgDto)
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(UserDto userDto) {
        Account account = getAccount(userDto);
        ProfileImg profileImg = getProfileImg(userDto);

        return User.builder()
                .account(account)
                .profileImg(profileImg)
                .build();
    }

    private static ProfileImgDto getProfileImgDto(User user) {
        ProfileImg profileImg = user.getProfileImg();
        ProfileImgDto profileImgDto = null;
        if (profileImg != null) {
            profileImgDto = ProfileImgDto.builder()
                    .originalName(profileImg.getOriginalName())
                    .storedName(profileImg.getStoredName())
                    .build();
        }
        return profileImgDto;
    }

    private static AccountDto getAccountDto(User user) {
        Account account = user.getAccount();
        AccountDto accountDto = null;
        if (account != null) {
            accountDto = AccountDto.builder()
                    .oauthId(account.getOauthId())
                    .provider(account.getProvider())
                    .email(account.getEmail())
                    .name(account.getName())
                    .profileImgUrl(account.getProfileImgUrl())
                    .build();
        }
        return accountDto;
    }

    private static ProfileImg getProfileImg(UserDto userDto) {
        ProfileImgDto profileImgDto = userDto.getProfileImgDto();
        ProfileImg profileImg = null;
        if (profileImgDto != null) {
            profileImg = ProfileImg.builder()
                    .originalName(profileImgDto.getOriginalName())
                    .storedName(profileImgDto.getStoredName())
                    .build();
        }
        return profileImg;
    }

    private static Account getAccount(UserDto userDto) {
        AccountDto accountDto = userDto.getAccountDto();
        Account account = null;
        if (accountDto != null) {
            account = Account.builder()
                    .oauthId(accountDto.getOauthId())
                    .provider(accountDto.getProvider())
                    .email(accountDto.getEmail())
                    .name(accountDto.getName())
                    .profileImgUrl(accountDto.getProfileImgUrl())
                    .build();
        }
        return account;
    }
}
