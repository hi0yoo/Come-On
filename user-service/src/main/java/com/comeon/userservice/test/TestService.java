package com.comeon.userservice.test;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TestService {

    private final UserRepository userRepository;

    public List<Long> initUser() {
        // 탈퇴하지 않은 유저만 토큰 초기화
        List<User> all = userRepository.findAll().stream().filter(User::isActivateUser).collect(Collectors.toList());
        if (all.size() >= 20) {
            return all.stream().map(User::getId).collect(Collectors.toList());
        }

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            users.add(
                    User.builder()
                            .account(
                                    UserAccount.builder()
                                            .oauthId("oauthId" + i)
                                            .provider(OAuthProvider.KAKAO)
                                            .email("email" + i + "@email.com")
                                            .name("userName" + i)
                                            .build()
                            )
                            .build()
            );
        }

        return userRepository.saveAll(users).stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}
