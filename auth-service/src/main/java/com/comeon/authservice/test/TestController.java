package com.comeon.authservice.test;

import com.comeon.authservice.common.jwt.JwtRepository;
import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.feign.userservice.UserServiceFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth-test-api")
public class TestController {

    private final UserServiceFeignClient testUserFeignClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @PostMapping("/init")
    public UserInitResponse initUsersAndTokens() {
        List<Long> userIds = testUserFeignClient.initUsers();

        UserInitResponse userInitResponse = new UserInitResponse();

        userIds.forEach(userId -> {
            JwtTokenInfo accessToken = jwtTokenProvider.createAccessToken(userId.toString(), "ROLE_USER");
            JwtTokenInfo refreshToken = jwtTokenProvider.createRefreshToken();
            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken.getValue(),
                    Duration.between(Instant.now(), refreshToken.getExpiry())
            );

            userInitResponse.getContents().add(
                    UserInitResponse.Data.builder()
                            .userId(userId)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build()
            );
        });

        return userInitResponse;
    }
}
