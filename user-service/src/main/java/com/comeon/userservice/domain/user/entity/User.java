package com.comeon.userservice.domain.user.entity;

import com.comeon.userservice.domain.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_img_id")
    private ProfileImg profileImg;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    public void authorize(Role role) {
        this.role = role;
    }

    @Builder
    public User(Account account, ProfileImg profileImg) {
        this.account = account;
        this.profileImg = profileImg;

        this.nickname = account.getName();

        this.role = Role.USER;
        this.status = Status.ACTIVATE;
    }

    public void withdrawal() {
        this.account = null;
        this.status = Status.WITHDRAWN;
    }

    public void updateNickname(String nickname) {
        if (nickname != null) {
            this.nickname = nickname;
        }
    }

    public void updateProfileImg(ProfileImg profileImg) {
        this.profileImg = profileImg;
//        profileImg.updateOriginalName(profileImg.getOriginalName());
//        profileImg.updateStoredName(profileImg.getStoredName());
    }

    public void deleteProfileImg() {
        this.profileImg = null;
    }
}
