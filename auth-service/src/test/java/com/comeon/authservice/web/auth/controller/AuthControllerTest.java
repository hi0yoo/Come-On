package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.config.TestConfig;
import com.comeon.authservice.domain.refreshtoken.dto.RefreshTokenDto;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.repository.RefreshTokenRepository;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.repository.UserRepository;
import com.comeon.authservice.web.auth.exception.handler.AuthControllerExceptionHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@Import(TestConfig.class)
class AuthControllerTest {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    AuthController authController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    MockMvc mockMvc;

    @BeforeEach
    void initMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(AuthControllerExceptionHandler.class)
                .build();
    }

    @Test
    @DisplayName("토큰 재발급 - AccessToken 만료, RefreshToken 7일 미만 남으면 둘 다 재발급 된다.")
    void reissueTokens() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);
        // AccessToken 만료 상태

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(3000))) // refreshToken 만료일 지정. 50분 추가
                .compact();
        // RefreshToken 유효 상태 - 만료까지 7일 미만 남음

        RefreshToken refreshToken = new RefreshToken(
                new RefreshTokenDto(
                        user,
                        refreshTokenValue
                )
        );
        refreshToken = refreshTokenRepository.save(refreshToken);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(300);

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("토큰 재발급 - AccessToken 만료, RefreshToken 7일 이상 남으면 AccessToken만 재발급 된다.")
    void reissueAccessTokenOnly() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);

        // AccessToken 만료 상태
        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(605000)))
                .compact();
        // RefreshToken 만료일 7일 이상 남은 상태

        RefreshToken refreshToken = new RefreshToken(
                new RefreshTokenDto(
                        user,
                        refreshTokenValue
                )
        );
        refreshToken = refreshTokenRepository.save(refreshToken);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(300);

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - AccessToken이 유효하면 요청이 실패하고 Http Status 400 반환한다.")
    void reissue_fail_accessToken_is_valid() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간으로 발행일 세팅
        Instant issuedAt = Instant.now();
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);
        // AccessToken 유효 상태

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
        );

        perform.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - AccessToken 만료, RefreshToken 검증에 실패하면 요청이 실패하고 Http Status 401 반환한다.")
    void reissue_fail_refreshToken_invalid() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);
        // AccessToken 만료 상태

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(605000)))
                .compact();
        // RefreshToken 만료일 7일 이상 남은 상태

        RefreshToken refreshToken = new RefreshToken(
                new RefreshTokenDto(
                        user,
                        refreshTokenValue
                )
        );
        refreshToken = refreshTokenRepository.save(refreshToken);

        String invalidRefreshToken = refreshToken.getToken() + "aa";

        Cookie refreshTokenCookie = new Cookie("refreshToken", invalidRefreshToken);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(300);

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - RefreshToken이 DB와 일치하지 않으면 요청이 실패하고 Http Status 401 반환한다.")
    void reissue_fail_refreshToken_invalid2() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);
        // AccessToken 만료 상태

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(605000)))
                .compact();
        // RefreshToken 만료일 7일 이상 남은 상태

        RefreshToken refreshToken = new RefreshToken(
                new RefreshTokenDto(
                        user,
                        refreshTokenValue
                )
        );
        refreshToken = refreshTokenRepository.save(refreshToken);

        String invalidRefreshToken = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("invalid")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(30000)))
                .compact();

        Cookie refreshTokenCookie = new Cookie("refreshToken", invalidRefreshToken);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(300);

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - AccessToken이 없으면 Http Status 400 반환한다.")
    void reissue_fail_no_accessToken() throws Exception {
        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        perform.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - AccessToken이 있고, RefreshToken이 없으면 Http Status 400 반환한다.")
    void reissue_fail_no_refreshToken() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);
        // AccessToken 만료 상태

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
        );

        perform.andExpect(status().isBadRequest());
    }
}