package com.comeon.userservice.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserStatus {

    ACTIVATE("활성화 된 유저입니다."),
    WITHDRAWN("탈퇴한 유저입니다."),
    ;

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }
}
