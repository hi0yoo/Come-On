package com.comeon.userservice.domain.user.service.dto;

import lombok.Getter;

@Getter
public class ModifyUserInfoFields {

    private String nickname;

    public ModifyUserInfoFields(String nickname) {
        this.nickname = nickname;
    }
}
