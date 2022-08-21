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

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    public void authorize(Role role) {
        this.role = role;
    }

    @Builder
    public User(Account account) {
        this.account = account;
        this.nickname = account.getName();
        this.profileImgUrl = account.getProfileImgUrl();
        this.role = Role.USER;
        this.status = Status.ACTIVATE;
    }

    public void withdrawal() {
        this.account = null;
        this.status = Status.WITHDRAWN;
    }
}
