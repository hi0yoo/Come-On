package com.comeon.userservice.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProfileImgDto {

    private Long id;
    private String originalName;
    private String storedName;

    @Builder
    public ProfileImgDto(Long id, String originalName, String storedName) {
        this.id = id;
        this.originalName = originalName;
        this.storedName = storedName;
    }
}
