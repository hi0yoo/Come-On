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

    private String oauthId;

    private OAuthProvider provider;

    private String email;

    private String name;

    private String profileImgUrl;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    public void authorize(Role role) {
        this.role = role;
    }

    @Builder
    public User(String oauthId, OAuthProvider provider, String email,
                String name, String profileImgUrl) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
        this.nickname = name;
        this.role = Role.USER;
    }

    public void updateOAuthInfo(String email, String name, String profileImgUrl) {
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }
}
