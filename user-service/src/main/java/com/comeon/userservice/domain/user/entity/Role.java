package com.comeon.userservice.domain.user.entity;

public enum Role {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
    ;

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getRoleValue() {
        return value;
    }
}