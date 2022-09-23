package com.comeon.authservice.feign.userservice.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSaveRequest {

    private String oauthId;
    private String provider;
    private String name;
    private String email;
    private String profileImgUrl;
}
