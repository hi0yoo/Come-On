package com.comeon.userservice.web.profileimage.response;

import lombok.Getter;

@Getter
public class ProfileImgRemoveResponse {

    public static final String SUCCESS_MESSAGE = "프로필 이미지 삭제가 완료되었습니다.";

    private String message;

    public ProfileImgRemoveResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public ProfileImgRemoveResponse(String message) {
        this.message = message;
    }
}
