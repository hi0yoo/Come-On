package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.common.jwt.JwtRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerTest {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    AuthController authController;

    @Autowired
    JwtRepository jwtRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    MockMvc mockMvc;

    Long userId;
    String userRole;
    String authorities;
    Instant accessTokenIssuedAt;
    Instant accessTokenExpiryAt;
    Instant refreshTokenIssuedAt;
    Instant refreshTokenExpiryAt;
    String accessToken;
    String refreshToken;

    void setAccessTokenCond(Instant issuedAt, Instant expiryAt) {
        this.accessTokenIssuedAt = issuedAt;
        this.accessTokenExpiryAt = expiryAt;
    }

    void setRefreshTokenCond(Instant issuedAt, Instant expiryAt) {
        this.refreshTokenIssuedAt = issuedAt;
        this.refreshTokenExpiryAt = expiryAt;
    }
    String createAccessToken() {
        return Jwts.builder()
                .setSubject(userId.toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(accessTokenIssuedAt))
                .setExpiration(Date.from(accessTokenExpiryAt))
                .compact();
    }

    String createRefreshToken() {
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(refreshTokenIssuedAt))
                .setExpiration(Date.from(refreshTokenExpiryAt))
                .compact();
    }

    @BeforeEach
    void initMockMvc(final WebApplicationContext context) throws ServletException {
        DelegatingFilterProxy delegateProxyFilter = new DelegatingFilterProxy();
        delegateProxyFilter.init(new MockFilterConfig(context.getServletContext(), BeanIds.SPRING_SECURITY_FILTER_CHAIN));

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .addFilters(delegateProxyFilter)
                .build();
    }

    @BeforeEach
    void initData() {
        this.userId = 1L;
        this.userRole = "ROLE_USER";
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(userRole)).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    @AfterEach
    void deleteData() {
        redisTemplate.delete("BLACKLIST_" + accessToken);
        jwtRepository.removeRefreshToken(userId.toString());
    }

    @Nested
    @DisplayName("토큰 재발급")
    class 토큰_재발급 {
        @Test
        @DisplayName("success - AccessToken 만료, RefreshToken 7일 미만 남으면 둘 다 재발급 된다.")
        void reissueTokens() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(500)
            );
            refreshToken = createRefreshToken();
            // RefreshToken 유효 상태 - 만료까지 7일 미만 남음

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.ofSeconds(refreshTokenExpiryAt.getEpochSecond())
            );

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(300);

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .cookie(refreshTokenCookie)
            );

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

            assertThat(jwtRepository.findRefreshTokenByUserId(userId.toString()).orElseThrow()).isNotEqualTo(refreshToken);
        }

        @Test
        @DisplayName("success - AccessToken 만료, RefreshToken 7일 이상 남으면 AccessToken만 재발급 된다.")
        void reissueAccessTokenOnly() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(605000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.ofSeconds(refreshTokenExpiryAt.getEpochSecond())
            );

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(300);

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .cookie(refreshTokenCookie)
            );

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

            assertThat(jwtRepository.findRefreshTokenByUserId(userId.toString()).orElseThrow()).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("fail - AccessToken이 유효하면 요청이 실패하고 Http Status 400 반환한다.")
        void reissue_fail_accessToken_is_valid() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            accessToken = createAccessToken();

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
            );

            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - AccessToken 만료, RefreshToken 검증에 실패하면 요청이 실패하고 Http Status 401 반환한다.")
        void reissue_fail_refreshToken_invalid() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.ofSeconds(refreshTokenExpiryAt.getEpochSecond())
            );

            String invalidRefreshToken = refreshToken + "aa";

            Cookie refreshTokenCookie = new Cookie("refreshToken", invalidRefreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(300);

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .cookie(refreshTokenCookie)
            );

            perform.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("fail - RefreshToken이 DB와 일치하지 않으면 요청이 실패하고 Http Status 401 반환한다.")
        void reissue_fail_refreshToken_invalid2() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(605000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.ofSeconds(refreshTokenExpiryAt.getEpochSecond())
            );

            String otherRefreshToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .setIssuer("invalid")
                    .setIssuedAt(Date.from(refreshTokenIssuedAt))
                    .setExpiration(Date.from(refreshTokenExpiryAt.plusSeconds(30000)))
                    .compact();

            Cookie refreshTokenCookie = new Cookie("refreshToken", otherRefreshToken);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(300);

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .cookie(refreshTokenCookie)
            );

            perform.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("fail - AccessToken이 없으면 Http Status 401 반환한다.")
        void reissue_fail_no_accessToken() throws Exception {
            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
            );

            perform.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("fail - AccessToken이 있고, RefreshToken이 없으면 Http Status 401 반환한다.")
        void reissue_fail_no_refreshToken() throws Exception {
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            ResultActions perform = mockMvc.perform(
                    post("/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
            );

            perform.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class 로그아웃 {

        @Test
        @DisplayName("success - 유효한 AccessToken이면 BlackList에 해당 AccessToken을 넣고, 사용자의 RefreshToken을 지운다.")
        void logout_success() throws Exception {
            // given
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(5000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
            );

            // when
            String requestAccessToken = accessToken;
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
            );

            // then
            perform.andExpect(status().isOk());
            String resultAccessToken = jwtRepository.findBlackList(accessToken).orElse(null);
            assertThat(resultAccessToken).isEqualTo(accessToken);
            assertThat(jwtRepository.findRefreshTokenByUserId(userId.toString()).isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fail - AccessToken이 유효하지 않으면 요청이 실패하고 Http Status 401 반환한다.")
        void logout_fail_1() throws Exception {
            // given
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(5000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
            );

            // when
            String requestAccessToken = accessToken + "asd";
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
            );

            // then
            perform.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("fail - AccessToken이 만료되었으면 요청이 실패하고 Http Status 401 반환한다.")
        void logout_fail_2() throws Exception {
            // given
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().minusSeconds(150)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(5000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
            );

            // when
            String requestAccessToken = accessToken;
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
            );

            // then
            perform.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("fail - AccessToken이 없으면 요청이 실패하고 Http Status 401 반환한다.")
        void logout_fail_3() throws Exception {
            // given
            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            perform.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class validateMe {

        @Test
        @DisplayName("토큰이 유효한 토큰이면 검증에 성공하고, 검증 성공 응답 메시지를 내린다.")
        void success() throws Exception {
            // given
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(5000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
            );

            // when
            String requestAccessToken = accessToken;
            ResultActions perform = mockMvc.perform(
                    post("/auth/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
            );

            String response = perform.andReturn().getResponse().getContentAsString();
            System.out.println(response);

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").exists());
        }

        @Test
        @DisplayName("토큰이 유효한 토큰이 아니면, 검증에 실패하고, 검증 실패 응답 메시지를 내린다.")
        void fail() throws Exception {
            // given
            setAccessTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(300)
            );
            accessToken = createAccessToken();

            setRefreshTokenCond(
                    Instant.now().minusSeconds(300),
                    Instant.now().plusSeconds(5000)
            );
            refreshToken = createRefreshToken();

            jwtRepository.addRefreshToken(
                    userId.toString(),
                    refreshToken,
                    Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
            );

            // when
            String invalidAccessToken = accessToken + "asd";
            ResultActions perform = mockMvc.perform(
                    post("/auth/validate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + invalidAccessToken)
            );

            perform.andExpect(status().isUnauthorized());
        }
    }
}