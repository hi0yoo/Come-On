package com.comeon.userservice.web.user.response;

import lombok.Getter;

@Getter
public class UserImageSaveResponse {

    private String profileImgUrl;

    public UserImageSaveResponse(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }
}
