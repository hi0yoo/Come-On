package com.comeon.userservice.domain.user.entity;

public enum UserRole {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
    ;

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getRoleValue() {
        return value;
    }
}