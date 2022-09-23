package com.comeon.userservice.domain.user.entity;

import com.comeon.userservice.domain.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    private String oauthId;

    private OAuthProvider provider;

    private String email;

    private String name;

    private String profileImgUrl;

    @Builder
    public UserAccount(String oauthId, OAuthProvider provider, String email, String name, String profileImgUrl) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }

    public void updateOAuthInfo(String email, String name, String profileImgUrl) {
        this.email = email;
        this.name = name;
        this.profileImgUrl = profileImgUrl;
    }
}
