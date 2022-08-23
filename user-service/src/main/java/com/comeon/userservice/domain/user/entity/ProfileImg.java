package com.comeon.userservice.domain.user.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImg {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_img_id")
    private Long id;

    private String originalName;

    private String storedName;

    @Builder
    public ProfileImg(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public void updateOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void updateStoredName(String storedName) {
        this.storedName = storedName;
    }

}
