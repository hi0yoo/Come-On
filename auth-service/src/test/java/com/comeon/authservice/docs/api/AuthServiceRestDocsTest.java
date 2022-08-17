package com.comeon.authservice.docs.api;

import com.comeon.authservice.config.TestConfig;
import com.comeon.authservice.docs.config.RestDocsSupport;
import com.comeon.authservice.docs.utils.RestDocsUtil;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.repository.UserRepository;
import com.comeon.authservice.auth.jwt.JwtRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
public class AuthServiceRestDocsTest extends RestDocsSupport {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtRepository jwtRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    User user;
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
                .setSubject(user.getId().toString())
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
    void initData() {
        this.user = userRepository.findById(1L).orElseThrow();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    @AfterEach
    void deleteData() {
        redisTemplate.delete("BLACKLIST_" + accessToken);
        jwtRepository.removeRefreshToken(user.getId().toString());
    }

    @Test
    @DisplayName("AccessToken, RefreshToken 모두 재발급")
    void reissueTokensSuccess() throws Exception {
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

        jwtRepository.addRefreshToken(
                user.getId().toString(),
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
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        perform.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Beaer AccessToken")
                        ),
                        RestDocsUtil.customRequestHeaders(
                                "cookie-request",
                                attributes(
                                        key("title").value("요청 쿠키"),
                                        key("name").value("Cookie"),
                                        key("cookie").value("refreshToken="),
                                        key("description").value("유효한 RefreshToken")
                                )
                        ),
                        RestDocsUtil.customResponseHeaders(
                                "cookie-response",
                                attributes(key("title").value("응답 쿠키")),
                                headerWithName(HttpHeaders.SET_COOKIE)
                                        .description("기존 RefreshToken 만료 기한이 7일 미만으로 남았다면 재발급하여 반환합니다.")
                                        .optional()
                                        .attributes(
                                                key("HttpOnly").value(true),
                                                key("cookie").value("refreshToken=; Path=/; Max-Age=; Expires=; HttpOnly")
                                        )
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급된 Access Token")
                        )
                )
        );
    }

    @Test
    @DisplayName("토큰 재발급 실패 - RefreshToken 만료")
    void reissueTokensFailExpiredRefreshToken() throws Exception {
        setAccessTokenCond(
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(150)
        );
        accessToken = createAccessToken();

        setRefreshTokenCond(
                Instant.now().minusSeconds(300),
                Instant.now().minusSeconds(150)
        );
        refreshToken = createRefreshToken();

        jwtRepository.addRefreshToken(
                user.getId().toString(),
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
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                        )
                )
        );
    }

    @Test
    @DisplayName("토큰 재발급 실패 - AccessToken 없음")
    void reissueTokensFailNoAccessToken() throws Exception {
        setRefreshTokenCond(
                Instant.now().minusSeconds(300),
                Instant.now().plusSeconds(500)
        );
        refreshToken = createRefreshToken();

        jwtRepository.addRefreshToken(
                user.getId().toString(),
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
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                        )
                )
        );
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutSuccess() throws Exception {
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
                user.getId().toString(),
                refreshToken,
                Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
        );

        // when
        String requestAccessToken = accessToken;
        ResultActions perform = mockMvc.perform(
                post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(org.apache.http.HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
        );

        // then
        perform.andExpect(status().isOk());
        String resultAccessToken = jwtRepository.findBlackList(accessToken).orElse(null);
        assertThat(resultAccessToken).isEqualTo(accessToken);
        assertThat(jwtRepository.findRefreshTokenByUserId(user.getId().toString()).isEmpty()).isTrue();

        perform.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Beaer AccessToken")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("로그아웃 성공 메시지")
                        )
                )
        );
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 AccessToken")
    void logoutFailInvalidAccessToken() throws Exception {
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
                user.getId().toString(),
                refreshToken,
                Duration.between(refreshTokenIssuedAt, refreshTokenExpiryAt)
        );

        // when
        String requestAccessToken = accessToken + "asd";
        ResultActions perform = mockMvc.perform(
                post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(org.apache.http.HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
        );

        // then
        perform.andExpect(status().isUnauthorized());

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                        )
                )
        );
    }
}
