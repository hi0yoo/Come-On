package com.comeon.userservice.domain.user.entity;

public enum UserRole {

    USER("ROLE_USER", "일반 유저의 권한을 갖습니다."),
    ADMIN("ROLE_ADMIN", "관리자의 권한을 갖습니다.")
    ;

    private final String value;
    private final String description;

    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getRoleValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}