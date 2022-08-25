package com.comeon.userservice.domain.profileimage.entity;

import com.comeon.userservice.domain.common.BaseTimeEntity;
import com.comeon.userservice.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImg extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_img_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String originalName;

    private String storedName;

    @Builder
    public ProfileImg(User user, String originalName, String storedName) {
        this.user = user;
        this.originalName = originalName;
        this.storedName = storedName;
        user.updateProfileImg(this);
    }

    public void updateOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void updateStoredName(String storedName) {
        this.storedName = storedName;
    }

}
