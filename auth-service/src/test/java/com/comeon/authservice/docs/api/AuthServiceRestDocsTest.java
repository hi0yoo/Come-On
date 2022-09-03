package com.comeon.authservice.docs.api;

import com.comeon.authservice.docs.config.RestDocsSupport;
import com.comeon.authservice.docs.utils.RestDocsUtil;
import com.comeon.authservice.common.jwt.JwtRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AuthServiceRestDocsTest extends RestDocsSupport {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    JwtRepository jwtRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

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

    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        String path = "/oauth2/authorize/{providerName}";
        ResultActions perform = mockMvc.perform(
                RestDocumentationRequestBuilders.get(path, "kakao")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("redirect_uri", "http://localhost:3000/front/redirect-page")
        );

        perform.andDo(
                document(
                        "{class-name}/{method-name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                attributes(key("title").value(path)),
                                parameterWithName("providerName").description("소셜 로그인 제공 벤더 ex) kakao")
                        ),
                        requestParameters(
                                attributes(key("title").value("쿼리 파라미터")),
                                parameterWithName("redirect_uri").description("로그인 성공시, 토큰을 전달받을, 프론트측 리다이렉트 URL")
                        )
                )
        );
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
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        perform.andDo(
                restDocs.document(
                        requestHeaders(
                                attributes(key("title").value("요청 헤더")),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입의 만료된 AccessToken")
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
                                attributes(key("title").value("응답 필드")),
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급된 Access Token"),
                                fieldWithPath("expiry").type(JsonFieldType.NUMBER).description("재발급된 Access Token의 만료일 - UNIX TIME")
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
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                attributes(key("title").value("응답 필드")),
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
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isUnauthorized());

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                attributes(key("title").value("응답 필드")),
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
                userId.toString(),
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
        assertThat(jwtRepository.findRefreshTokenByUserId(userId.toString()).isEmpty()).isTrue();

        perform.andDo(
                restDocs.document(
                        requestHeaders(
                                attributes(key("title").value("요청 헤더")),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입의 유효한 AccessToken")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                attributes(key("title").value("응답 필드")),
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
                userId.toString(),
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
                                attributes(key("title").value("응답 필드")),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                        )
                )
        );
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
                            .header(org.apache.http.HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
            );

            String response = perform.andReturn().getResponse().getContentAsString();
            System.out.println(response);

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").value(userId));

            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 타입의 유효한 AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("현재 이용중인 유저의 식별값")
                            )
                    )
            );
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
                            .header(org.apache.http.HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + invalidAccessToken)
            );

            perform.andExpect(status().isUnauthorized());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }
}
