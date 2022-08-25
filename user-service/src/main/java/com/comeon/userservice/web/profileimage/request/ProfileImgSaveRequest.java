package com.comeon.userservice.web.profileimage.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
public class ProfileImgSaveRequest {

    @NotNull
    MultipartFile imgFile;

    public ProfileImgSaveRequest(MultipartFile imgFile) {
        this.imgFile = imgFile;
    }
}
