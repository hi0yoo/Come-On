package com.comeon.userservice.domain.profileimage.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProfileImgDto {

    private String originalName;
    private String storedName;

    @Builder
    public ProfileImgDto(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }
}
