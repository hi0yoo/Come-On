package com.comeon.authservice.domain.user.entity;

import com.comeon.authservice.domain.BaseTimeEntity;
import com.comeon.authservice.domain.user.dto.UserDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    private String oauthId;

    private String email;

    private String name;

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public User(String oauthId, String email, String name,
                String nickname, String profileImgUrl,
                OAuthProvider provider) {
        this.oauthId = oauthId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.provider = provider;
        this.role = Role.USER;
    }

    public User(UserDto userDto) {
        this.oauthId = userDto.getOauthId();
        this.email = userDto.getEmail();
        this.name = userDto.getName();
        this.nickname = userDto.getName();
        this.profileImgUrl = userDto.getProfileImgUrl();
        this.provider = OAuthProvider.valueOf(userDto.getProviderName().toUpperCase());
        this.role = Role.USER;
    }

    public void updateOAuthInfo(String email, String name, String profileImgUrl) {
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

    public void updateOAuthInfo(UserDto userDto) {
        this.email = userDto.getEmail();
        this.name = userDto.getName();
        this.profileImgUrl = userDto.getProfileImgUrl();
    }
}
