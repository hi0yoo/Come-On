package com.comeon.userservice.domain.user.entity;

import com.comeon.userservice.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String oAuthId;

    private String email;

    private String name;

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(Long id, String oAuthId, String email, String name,
                String nickname, String profileImgUrl,
                OAuthProvider provider) {
        this.id = id;
        this.oAuthId = oAuthId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.provider = provider;
        this.role = Role.USER;
    }

    public void updateOAuthInfo(String email, String name, String profileImgUrl) {
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

    public void authorize(Role role) {
        this.role = role;
    }
}
