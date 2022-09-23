package com.comeon.userservice.domain.user.entity;

import com.comeon.userservice.domain.common.BaseTimeEntity;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
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
    private UserAccount account;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private ProfileImg profileImg;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public void authorize(UserRole role) {
        this.role = role;
    }

    @Builder
    public User(UserAccount account) {
        this.account = account;

        this.nickname = account.getName();

        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVATE;
    }

    public void withdrawal() {
        this.account = null;
        this.status = UserStatus.WITHDRAWN;
    }

    public void updateNickname(String nickname) {
        if (nickname != null) {
            this.nickname = nickname;
        }
    }

    public boolean isActivateUser() {
        return this.status == UserStatus.ACTIVATE;
    }

    public void updateProfileImg(ProfileImg profileImg) {
        this.profileImg = profileImg;
    }
}
