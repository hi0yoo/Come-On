package com.comeon.authservice.config;

import com.comeon.authservice.domain.user.entity.OAuthProvider;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.event.EventListener;

@TestConfiguration
public class TestConfig {

    @Autowired
    UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        User user = new User("12345",
                "user1@email.com",
                "user1",
                "user1",
                "user1.profileImg",
                OAuthProvider.KAKAO
        );
        user = userRepository.save(user);
    }
}
