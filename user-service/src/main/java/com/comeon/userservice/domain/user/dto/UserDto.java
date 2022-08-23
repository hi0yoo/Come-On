package com.comeon.userservice.domain.user.dto;

import com.comeon.userservice.domain.user.entity.Role;
import com.comeon.userservice.domain.user.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {

    private Long id;
    private AccountDto accountDto;
    private ProfileImgDto profileImgDto;
    private String nickname;
    private Role role;
    private Status status;

    @Builder
    public UserDto(Long id, AccountDto accountDto, ProfileImgDto profileImgDto, String nickname, Role role, Status status) {
        this.id = id;
        this.accountDto = accountDto;
        this.profileImgDto = profileImgDto;

        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

}
