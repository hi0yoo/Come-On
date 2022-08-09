package com.comeon.authservice.domain.refreshtoken.entity;

import com.comeon.authservice.domain.BaseTimeEntity;
import com.comeon.authservice.domain.refreshtoken.dto.RefreshTokenDto;
import com.comeon.authservice.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    public RefreshToken(RefreshTokenDto refreshTokenDto) {
        this.user = refreshTokenDto.getUser();
        this.token = refreshTokenDto.getRefreshToken();
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
