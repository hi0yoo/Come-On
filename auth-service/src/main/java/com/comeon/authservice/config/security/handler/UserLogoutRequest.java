package com.comeon.authservice.config.security.handler;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class UserLogoutRequest implements Serializable {

    private String accessToken;
    private String frontRedirectUri;

    public UserLogoutRequest(String accessToken, String frontRedirectUri) {
        this.accessToken = accessToken;
        this.frontRedirectUri = frontRedirectUri;
    }
}
