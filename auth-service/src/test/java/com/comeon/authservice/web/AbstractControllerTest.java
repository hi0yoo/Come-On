package com.comeon.authservice.web;

import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.web.docs.config.RestDocsSupport;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

@Testcontainers
public abstract class AbstractControllerTest extends RestDocsSupport {

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected JwtTokenInfo generateAccessToken(Long userId, String role, Instant issuedAt, Instant expiryAt) {
        String authorities = Collections.singletonList(new SimpleGrantedAuthority(role)).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String accessTokenValue = Jwts.builder()
                .setSubject(userId.toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryAt))
                .compact();
        return new JwtTokenInfo(accessTokenValue, expiryAt);
    }

    protected JwtTokenInfo generateRefreshToken(Instant issuedAt, Instant expiryAt) {
        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryAt))
                .compact();

        return new JwtTokenInfo(refreshTokenValue, expiryAt);
    }

    private static final String DOCKER_REDIS_IMAGE = "redis:7.0.4";

    @ClassRule
    static final GenericContainer REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer(DOCKER_REDIS_IMAGE)
                .withExposedPorts(6379)
                .withReuse(true);

        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }
}
