package com.comeon.userservice.web.profileimage.response;

import lombok.Getter;

@Getter
public class ProfileImgSaveResponse {

    private Long profileImgId;
    private String imageUrl;

    public ProfileImgSaveResponse(Long profileImgId, String imageUrl) {
        this.profileImgId = profileImgId;
        this.imageUrl = imageUrl;
    }
}
