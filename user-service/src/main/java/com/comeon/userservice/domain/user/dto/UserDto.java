package com.comeon.userservice.domain.user.dto;

import com.comeon.userservice.domain.user.entity.Role;
import com.comeon.userservice.domain.user.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserDto {

    private Long id;
    private AccountDto accountDto;
    private String nickname;
    private String profileImgUrl;
    private Role role;
    private Status status;

    @Builder
    public UserDto(Long id, AccountDto accountDto, String nickname, String profileImgUrl, Role role, Status status) {
        this.id = id;
        this.accountDto = accountDto;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.role = role;
        this.status = status;
    }
}
