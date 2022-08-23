package com.comeon.userservice.web.user.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
public class UserProfileImgSaveRequest {

    @NotNull
    MultipartFile imgFile;

    public UserProfileImgSaveRequest(MultipartFile imgFile) {
        this.imgFile = imgFile;
    }
}
