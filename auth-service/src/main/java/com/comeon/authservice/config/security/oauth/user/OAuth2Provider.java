package com.comeon.authservice.config.security.oauth.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum OAuth2Provider {
    KAKAO("카카오"),
    ;

    private final String description;

    OAuth2Provider(String description) {
        this.description = description;
    }

    @JsonCreator
    public static OAuth2Provider from(String value) {
        return Stream.of(values())
                .filter(provider -> provider.name().equals(value.toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
