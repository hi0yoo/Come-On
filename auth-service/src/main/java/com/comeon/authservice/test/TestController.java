package com.comeon.authservice.test;

import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.feign.userservice.UserServiceFeignClient;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth-test-api")
public class TestController {

    private final UserServiceFeignClient testUserFeignClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository jwtRepository;

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @PostMapping("/init")
    public UserInitResponse initUsersAndTokens() {
        List<Long> userIds = testUserFeignClient.initUsers();

        UserInitResponse userInitResponse = new UserInitResponse();

        userIds.forEach(userId -> {
            String userRole = "ROLE_USER";

            // 정상 AccessToken
            JwtTokenInfo accessToken = jwtTokenProvider.createAccessToken(userId.toString(), userRole);

            String authorities = Collections.singletonList(new SimpleGrantedAuthority(userRole)).stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            Instant instant = Instant.now().minusSeconds(60);
            // 만료된 AccessToken(재발급 확인용)
            JwtTokenInfo expiredAccessToken = new JwtTokenInfo(
                    Jwts.builder()
                            .setSubject(userId.toString())
                            .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                            .claim("auth", authorities)
                            .setIssuer("come-on-server")
                            .setIssuedAt(Date.from(instant))
                            .setExpiration(Date.from(instant))
                            .compact(),
                    instant
            );

            // 정상 refreshToken
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
                            .expiredAccessToken(expiredAccessToken)
                            .build()
            );
        });

        return userInitResponse;
    }
}
