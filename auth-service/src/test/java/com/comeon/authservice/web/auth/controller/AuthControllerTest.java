package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.exception.ErrorCode;
import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.comeon.authservice.common.utils.CookieUtil;
import com.comeon.authservice.config.security.handler.UserLogoutRequest;
import com.comeon.authservice.web.AbstractControllerTest;
import com.comeon.authservice.web.controller.AuthController;
import com.comeon.authservice.web.docs.utils.RestDocsUtil;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockCookie;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;

import static com.comeon.authservice.common.utils.CookieUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerTest extends AbstractControllerTest {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Autowired
    AuthController authController;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void deleteData() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushAll();
            return null;
        });
    }

    @Nested
    @DisplayName("로그인")
    class login {

        @Test
        @DisplayName("로그인")
        void request() throws Exception {
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
                                    parameterWithName("providerName").description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.OAUTH_PROVIDER_CODE))
                            ),
                            requestParameters(
                                    attributes(key("title").value("쿼리 파라미터")),
                                    parameterWithName("redirect_uri").description("로그인 성공시, 토큰을 전달받을, 프론트측 리다이렉트 URL")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("JwtAuthenticationFilter 요청")
    class jwtAuthenticationFilterRequests {

        @Nested
        @DisplayName("토큰 검증")
        class validateMe {

            @Test
            @DisplayName("토큰이 유효한 토큰이면 검증에 성공하고, 검증 성공 응답 메시지를 내린다.")
            void success() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, Instant.now(), Instant.now().plusSeconds(300));

                // when
                String requestAccessToken = accessTokenInfo.getValue();
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
                );

                // then
                perform.andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.userId").value(userId));

                // docs
                perform.andDo(
                        restDocs.document(
                                requestHeaders(
                                        attributes(key("title").value("요청 헤더")),
                                        headerWithName(org.springframework.http.HttpHeaders.AUTHORIZATION).description("Bearer 타입의 유효한 AccessToken")
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
            @DisplayName("만료된 토큰일 경우, http status 401 반환. ErrorCode.INVALID_ACCESS_TOKEN")
            void expiredAccessToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                Instant expired = Instant.now().minusSeconds(300);
                JwtTokenInfo expiredAccessTokenInfo = generateAccessToken(userId, userRole, expired, expired);

                // when
                String invalidAccessToken = expiredAccessTokenInfo.getValue();
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + invalidAccessToken)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("엑세스 토큰 값이 잘못되어서 검증에 실패한 경우, 401 error 반환. ErrorCode.INVALID_ACCESS_TOKEN")
            void invalidAccessToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                JwtTokenInfo accessToken = generateAccessToken(userId, userRole, Instant.now(), Instant.now().plusSeconds(300));

                // when
                String invalidAccessTokenValue = accessToken.getValue() + "asd";
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + invalidAccessTokenValue)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더가 없는 경우, http status 401 반환. ErrorCode.NO_AUTHORIZATION_HEADER")
            void noAuthorizationHeader() throws Exception {
                // when
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_AUTHORIZATION_HEADER.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORIZATION_HEADER.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더는 있지만 값이 없는 경우, http status 401 반환. ErrorCode.NO_AUTHORIZATION_HEADER")
            void noAccessToken() throws Exception {
                // when
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "")
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_AUTHORIZATION_HEADER.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORIZATION_HEADER.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더의 토큰이 'Bearer '로 시작하지 않는 경우, http status 401 반환. ErrorCode.NOT_SUPPORTED_TOKEN_TYPE")
            void accessTokenIsNotBearerType() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                JwtTokenInfo accessToken = generateAccessToken(userId, userRole, Instant.now(), Instant.now().plusSeconds(300));

                // when
                String accessTokenValue = accessToken.getValue();
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, accessTokenValue)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NOT_SUPPORTED_TOKEN_TYPE.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NOT_SUPPORTED_TOKEN_TYPE.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken이 레디스 로그아웃 블랙리스트에 있는 경우, http status 401 반환. ErrorCode.INVALID_ACCESS_TOKEN")
            void includeBlackList() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                JwtTokenInfo accessToken = generateAccessToken(userId, userRole, Instant.now(), Instant.now().plusSeconds(300));

                String accessTokenValue = accessToken.getValue();
                // 블랙리스트에 AccessToken 추가
                redisRepository.addBlackList(accessTokenValue, Duration.between(Instant.now(), accessToken.getExpiry()));

                // when
                ResultActions perform = mockMvc.perform(
                        get("/auth/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }
        }
    }

    @Nested
    @DisplayName("ReissueAuthenticationFilter 요청")
    class reissueAuthenticationFilterRequests {

        @Nested
        @DisplayName("토큰 재발급")
        class reissueTokens {

            private Cookie generateRefreshTokenCookie(String refreshTokenValue) {
                Cookie refreshTokenCookie = new Cookie("refreshToken", refreshTokenValue);
                refreshTokenCookie.setPath("/");
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setMaxAge(300);
                return refreshTokenCookie;
            }

            @Test
            @DisplayName("AccessToken 만료, RefreshToken은 유효하고, RefreshToken 만료일이 7일 미만 남았으면, 둘 다 재발급 한다.")
            void reissueAllTokens() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // accessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 미만 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                // 레디스에 refreshToken 저장
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue());
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                        .andExpect(jsonPath("$.data.expiry").isNotEmpty())
                        .andExpect(jsonPath("$.data.userId").isNotEmpty())
                        .andExpect(cookie().exists("refreshToken"));

                // docs
                perform.andDo(
                        restDocs.document(
                                requestHeaders(
                                        attributes(key("title").value("요청 헤더")),
                                        headerWithName(org.springframework.http.HttpHeaders.AUTHORIZATION).description("Bearer 타입의 만료된 AccessToken")
                                ),
                                RestDocsUtil.customRequestHeaders(
                                        "cookie-request",
                                        attributes(
                                                key("title").value("요청 쿠키"),
                                                key("name").value("Cookie"),
                                                key("cookie").value("refreshToken"),
                                                key("description").value("유효한 RefreshToken")
                                        )
                                ),
                                RestDocsUtil.customResponseHeaders(
                                        "cookie-response",
                                        attributes(key("title").value("응답 쿠키")),
                                        headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                                .description("기존 RefreshToken 만료 기한이 7일 미만으로 남았다면 재발급하여 반환합니다.")
                                                .optional()
                                                .attributes(
                                                        key("HttpOnly").value(true),
                                                        key("cookie").value("refreshToken"),
                                                        key("Secure").value(true),
                                                        key("SameSite").value("NONE")
                                                )
                                ),
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급된 Access Token"),
                                        fieldWithPath("expiry").type(JsonFieldType.NUMBER).description("재발급된 Access Token의 만료일 - UNIX TIME"),
                                        fieldWithPath("userId").type(JsonFieldType.NUMBER).description("재발급 요청한 유저의 식별값")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken 만료, RefreshToken 7일 이상 남으면 AccessToken만 재발급 된다.")
            void reissueAccessTokenOnly() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 이상 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue());
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                        .andExpect(jsonPath("$.data.expiry").isNotEmpty())
                        .andExpect(jsonPath("$.data.userId").isNotEmpty())
                        .andExpect(cookie().doesNotExist("refreshToken"));

                // docs
                perform.andDo(
                        restDocs.document(
                                requestHeaders(
                                        attributes(key("title").value("요청 헤더")),
                                        headerWithName(org.springframework.http.HttpHeaders.AUTHORIZATION).description("Bearer 타입의 만료된 AccessToken")
                                ),
                                RestDocsUtil.customRequestHeaders(
                                        "cookie-request",
                                        attributes(
                                                key("title").value("요청 쿠키"),
                                                key("name").value("Cookie"),
                                                key("cookie").value("refreshToken"),
                                                key("description").value("유효한 RefreshToken")
                                        )
                                ),
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("재발급된 Access Token"),
                                        fieldWithPath("expiry").type(JsonFieldType.NUMBER).description("재발급된 Access Token의 만료일 - UNIX TIME"),
                                        fieldWithPath("userId").type(JsonFieldType.NUMBER).description("재발급 요청한 유저의 식별값")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken 검증시 만료 예외가 아닌 다른 예외가 발생하면, http status 401 발생. ErrorCode.INVALID_ACCESS_TOKEN")
            void invalidAccessToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 이상 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String invalidAccessToken = accessTokenInfo.getValue() + "asd";
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue());
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + invalidAccessToken)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken이 유효하면 요청이 실패하고 Http Status 400 반환한다. ErrorCode.NOT_EXPIRED_ACCESS_TOKEN")
            void notExpiredAccessToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 유효 상태
                Instant accessTokenIssuedAt = Instant.now();
                Instant accessTokenExpiredAt = accessTokenIssuedAt.plusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenIssuedAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 이상 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue());
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NOT_EXPIRED_ACCESS_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NOT_EXPIRED_ACCESS_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken 만료, refreshToken이 요청 쿠키에 없을 경우, 요청이 실패하고 http status 401 반환한다. ErrorCode.NO_REFRESH_TOKEN")
            void noRefreshToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_REFRESH_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_REFRESH_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }


            @Test
            @DisplayName("AccessToken 만료, RefreshToken 검증에 실패하면 요청이 실패하고 Http Status 401 반환한다. ErrorCode.INVALID_REFRESH_TOKEN")
            void invalidRefreshToken() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 이상 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue() + "asd");
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("AccessToken 만료상태여도 RefreshToken이 redis와 일치하지 않으면 요청이 실패하고 Http Status 401 반환한다. ErrorCode.INVALID_REFRESH_TOKEN")
            void refreshTokenDoesNotMatchRedis() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // RefreshToken 유효 상태 - 만료까지 7일 이상 남음
                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                // RefreshToken redis에 저장하지 않음 -> 필터 검증 오류 발생!

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                Cookie refreshTokenCookie = generateRefreshTokenCookie(refreshTokenInfo.getValue());
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessTokenValue)
                                .cookie(refreshTokenCookie)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더가 없으면 Http Status 401 반환한다. ErrorCode.NO_AUTHORIZATION_HEADER")
            void noAuthorizationHeader() throws Exception {
                // when
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_AUTHORIZATION_HEADER.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORIZATION_HEADER.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더가 있으나, 값이 비어있으면 Http Status 401 반환한다. ErrorCode.NO_AUTHORIZATION_HEADER")
            void emptyAuthorizationHeader() throws Exception {
                // when
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "")
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_AUTHORIZATION_HEADER.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORIZATION_HEADER.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }

            @Test
            @DisplayName("Authorization 헤더가 'Bearer '로 시작하지 않으면, http status 401 반환. ErrorCode.NOT_SUPPORTED_TOKEN_TYPE")
            void notSupportedTokenType() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                // AccessToken 만료 상태
                Instant accessTokenExpiredAt = Instant.now().minusSeconds(300);
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, accessTokenExpiredAt, accessTokenExpiredAt);

                // when
                String accessTokenValue = accessTokenInfo.getValue();
                ResultActions perform = mockMvc.perform(
                        post("/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, accessTokenValue)
                );

                // then
                perform.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.data.code").value(ErrorCode.NOT_SUPPORTED_TOKEN_TYPE.getCode()))
                        .andExpect(jsonPath("$.data.message").value(ErrorCode.NOT_SUPPORTED_TOKEN_TYPE.getMessage()));

                // docs
                perform.andDo(
                        restDocs.document(
                                responseFields(
                                        beneathPath("data").withSubsectionId("data"),
                                        attributes(key("title").value("응답 필드")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                )
                        )
                );
            }
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class logout {

        @Test
        @DisplayName("소셜 로그아웃 페이지 리다이렉트")
        void redirectSocialLogout() throws Exception {
            // given
            JwtTokenInfo accessTokenInfo = generateAccessToken(1L, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();
            String redirectUri = "http://localhost:3000";

            // when
            ResultActions perform = mockMvc.perform(
                    post("/oauth2/logout")
                            .param("token", accessToken)
                            .param("redirect_uri", redirectUri)
            );

            // then
            perform.andExpect(status().is3xxRedirection())
                    .andExpect(cookie().exists(CookieUtil.COOKIE_NAME_USER_LOGOUT_REQUEST));

            // docs
            perform.andDo(
                    restDocs.document(
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("token").description("로그인 또는 재발급을 통해 발급받은 유효한 엑세스 토큰값"),
                                    parameterWithName("redirect_uri").description("로그아웃 과정을 완료하고 리다이렉트 할 주소")
                            ),
                            RestDocsUtil.customResponseHeaders(
                                    "cookie-response",
                                    attributes(key("title").value("응답 쿠키")),
                                    headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                            .description("소셜 로그아웃 이후, 서버 로그아웃에서 검증 및 사용될 정보들을 담은 쿠키. 요청 파라미터로 입력한 token, redirect_uri 포함.")
                                            .attributes(
                                                    key("HttpOnly").value(true),
                                                    key("cookie").value("logoutRequest"),
                                                    key("Secure").value(true),
                                                    key("SameSite").value("NONE")
                                            )
                            )
                    )
            );
        }

        @Test
        @DisplayName("서버 로그아웃 호출 성공")
        void serverLogoutSuccess() throws Exception {
            // given
            JwtTokenInfo accessTokenInfo = generateAccessToken(1L, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();
            String redirectUri = "http://localhost:3000";

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(accessToken, redirectUri);
            ResponseCookie responseCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(MockCookie.parse(responseCookie.toString()))
            );

            // then
            perform.andExpect(status().is3xxRedirection())
                    .andExpect(cookie().value(COOKIE_NAME_USER_LOGOUT_REQUEST, ""))
                    .andExpect(cookie().maxAge(COOKIE_NAME_USER_LOGOUT_REQUEST, 0))
                    .andExpect(cookie().doesNotExist(COOKIE_NAME_REFRESH_TOKEN));

            // docs
            perform.andDo(
                    restDocs.document(
                            RestDocsUtil.customRequestHeaders(
                                    "cookie-request",
                                    attributes(
                                            key("title").value("요청 쿠키"),
                                            key("name").value(org.springframework.http.HttpHeaders.COOKIE),
                                            key("cookie").value("logoutRequest"),
                                            key("description").value("로그아웃에서 검증 및 사용될 정보들을 담은 쿠키. 소셜 로그아웃 API 호출시, 요청 파라미터로 입력한 token, redirect_uri 포함하여 자동 생성.")
                                    )
                            ),
                            RestDocsUtil.customResponseHeaders(
                                    "cookie-response",
                                    attributes(key("title").value("응답 쿠키")),
                                    headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                            .description("로그아웃 요청 쿠키 삭제")
                                            .attributes(
                                                    key("HttpOnly").value(true),
                                                    key("cookie").value("logoutRequest"),
                                                    key("Secure").value(true),
                                                    key("SameSite").value("NONE")
                                            )
                            )
                    )
            );
        }

        @Test
        @DisplayName("서버 로그아웃 호출 성공. 리프레시 토큰 쿠키가 있으면 리프레시 토큰 쿠키를 삭제한다. 레디스에서도 지운다. 엑세스 토큰은 블랙리스트로 등록된다.")
        void removeRefreshTokenCookie() throws Exception {
            // given
            long userId = 1L;
            JwtTokenInfo accessTokenInfo = generateAccessToken(userId, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();
            String redirectUri = "http://localhost:3000";

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(accessToken, redirectUri);
            ResponseCookie logoutRequestCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            JwtTokenInfo refreshTokenInfo = generateRefreshToken(Instant.now(), Instant.now().plusSeconds(600));
            redisRepository.addRefreshToken(
                    String.valueOf(userId),
                    refreshTokenInfo.getValue(),
                    Duration.between(
                            Instant.now(),
                            Instant.now().plusSeconds(600)
                    )
            );
            ResponseCookie refreshTokenCookie = ResponseCookie.from(COOKIE_NAME_REFRESH_TOKEN, refreshTokenInfo.getValue())
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(
                                    MockCookie.parse(logoutRequestCookie.toString()),
                                    MockCookie.parse(refreshTokenCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().is3xxRedirection())
                    .andExpect(cookie().value(COOKIE_NAME_USER_LOGOUT_REQUEST, ""))
                    .andExpect(cookie().maxAge(COOKIE_NAME_USER_LOGOUT_REQUEST, 0))
                    .andExpect(cookie().value(COOKIE_NAME_REFRESH_TOKEN, ""))
                    .andExpect(cookie().maxAge(COOKIE_NAME_REFRESH_TOKEN, 0));

            assertThat(redisRepository.findRefreshTokenByUserId(String.valueOf(userId)))
                    .isNotPresent();
            assertThat(redisRepository.findBlackList(accessToken))
                    .isPresent();

            // docs
            perform.andDo(
                    restDocs.document(
                            RestDocsUtil.customRequestHeaders(
                                    "cookie-request",
                                    attributes(
                                            key("title").value("요청 쿠키"),
                                            key("name").value(org.springframework.http.HttpHeaders.COOKIE),
                                            key("cookie").value(COOKIE_NAME_USER_LOGOUT_REQUEST),
                                            key("description").value("로그아웃에서 검증 및 사용될 정보들을 담은 쿠키. 소셜 로그아웃 API 호출시, 요청 파라미터로 입력한 token, redirect_uri 포함하여 자동 생성.")
                                    )
                            ),
                            RestDocsUtil.customResponseHeaders(
                                    "cookie-response",
                                    attributes(key("title").value("응답 쿠키")),
                                    headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                            .description("로그아웃 요청 쿠키 삭제")
                                            .attributes(
                                                    key("HttpOnly").value(true),
                                                    key("cookie").value(COOKIE_NAME_USER_LOGOUT_REQUEST),
                                                    key("Secure").value(true),
                                                    key("SameSite").value("NONE")
                                            ),
                                    headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                            .description("리프레시 토큰 쿠키 삭제")
                                            .attributes(
                                                    key("HttpOnly").value(true),
                                                    key("cookie").value(COOKIE_NAME_REFRESH_TOKEN),
                                                    key("Secure").value(true),
                                                    key("SameSite").value("NONE")
                                            )
                            )
                    )
            );
        }

        @Test
        @DisplayName("서버 로그아웃 호출 성공. logoutRequest 쿠키가 없으면 요청 파라미터를 통해 logoutRequest를 생성한다.")
        void logoutRequestInParameter() throws Exception {
            // given
            long userId = 1L;
            JwtTokenInfo accessTokenInfo = generateAccessToken(userId, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();
            String redirectUri = "http://localhost:3000";

            JwtTokenInfo refreshTokenInfo = generateRefreshToken(Instant.now(), Instant.now().plusSeconds(600));
            redisRepository.addRefreshToken(
                    String.valueOf(userId),
                    refreshTokenInfo.getValue(),
                    Duration.between(
                            Instant.now(),
                            Instant.now().plusSeconds(600)
                    )
            );
            ResponseCookie refreshTokenCookie = ResponseCookie.from(COOKIE_NAME_REFRESH_TOKEN, refreshTokenInfo.getValue())
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .param("token", accessToken)
                            .param("redirect_uri", redirectUri)
                            .cookie(
                                    MockCookie.parse(refreshTokenCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().is3xxRedirection())
                    .andExpect(cookie().doesNotExist(COOKIE_NAME_USER_LOGOUT_REQUEST))
                    .andExpect(cookie().value(COOKIE_NAME_REFRESH_TOKEN, ""))
                    .andExpect(cookie().maxAge(COOKIE_NAME_REFRESH_TOKEN, 0));

            assertThat(redisRepository.findRefreshTokenByUserId(String.valueOf(userId)))
                    .isNotPresent();
            assertThat(redisRepository.findBlackList(accessToken))
                    .isPresent();

            // docs
            perform.andDo(
                    restDocs.document(
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("token").description("로그인 또는 재발급을 통해 발급받은 유효한 엑세스 토큰값"),
                                    parameterWithName("redirect_uri").description("로그아웃 과정을 완료하고 리다이렉트 할 주소")
                            ),
                            RestDocsUtil.customResponseHeaders(
                                    "cookie-response",
                                    attributes(key("title").value("응답 쿠키")),
                                    headerWithName(org.springframework.http.HttpHeaders.SET_COOKIE)
                                            .description("리프레시 토큰 쿠키 삭제")
                                            .attributes(
                                                    key("HttpOnly").value(true),
                                                    key("cookie").value(COOKIE_NAME_REFRESH_TOKEN),
                                                    key("Secure").value(true),
                                                    key("SameSite").value("NONE")
                                            )
                            )
                    )
            );
        }

        @Test
        @DisplayName("쿠키와 요청 파라미터 모두 logoutRequest가 없으면 401 반환한다. ErrorCode.NO_PARAM_TOKEN")
        void noLogoutRequest() throws Exception {
            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
            );

            // then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_PARAM_TOKEN.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_PARAM_TOKEN.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("param-logoutRequest에 엑세스 토큰이 없으면 401 반환한다. ErrorCode.NO_PARAM_TOKEN")
        void noAccessTokenParam() throws Exception {
            // given
            String redirectUri = "http://localhost:3000";

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .param("redirect_uri", redirectUri)
            );

            // then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_PARAM_TOKEN.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_PARAM_TOKEN.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("param-logoutRequest에 redirectUri 없으면 400 반환한다. ErrorCode.NO_PARAM_REDIRECT_URI")
        void noRedirectUriParam() throws Exception {
            // given
            long userId = 1L;
            JwtTokenInfo accessTokenInfo = generateAccessToken(userId, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .param("token", accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_PARAM_REDIRECT_URI.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_PARAM_REDIRECT_URI.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("cookie-logoutRequest에 redirectUri 없으면 400 반환한다. ErrorCode.NO_PARAM_REDIRECT_URI")
        void noRedirectUriInCookie() throws Exception {
            // given
            long userId = 1L;
            JwtTokenInfo accessTokenInfo = generateAccessToken(userId, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(accessToken, null);
            ResponseCookie logoutRequestCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(
                                    MockCookie.parse(logoutRequestCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_PARAM_REDIRECT_URI.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_PARAM_REDIRECT_URI.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("cookie-logoutRequest에 엑세스 토큰이 없으면 401 반환한다. ErrorCode.NO_PARAM_TOKEN")
        void noAccessTokenInCookie() throws Exception {
            // given
            String redirectUri = "http://localhost:3000";

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(null, redirectUri);
            ResponseCookie logoutRequestCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(
                                    MockCookie.parse(logoutRequestCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_PARAM_TOKEN.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_PARAM_TOKEN.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("accessToken이 블랙리스트에 있다면 401 반환한다. ErrorCode.INVALID_ACCESS_TOKEN")
        void accessTokenInBlackList() throws Exception {
            // given
            long userId = 1L;
            JwtTokenInfo accessTokenInfo = generateAccessToken(userId, "USER_ROLE", Instant.now(), Instant.now().plusSeconds(300));
            String accessToken = accessTokenInfo.getValue();
            String redirectUri = "http://localhost:3000";

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(accessToken, redirectUri);
            ResponseCookie logoutRequestCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            redisRepository.addBlackList(accessToken, Duration.between(Instant.now(), Instant.now().plusSeconds(60)));

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(
                                    MockCookie.parse(logoutRequestCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("accessToken 검증에 실패하면 401 반환한다. ErrorCode.INVALID_ACCESS_TOKEN")
        void validFailAccessToken() throws Exception {
            // given
            long userId = 1L;
            String accessToken = "aewhfbajwefbajewhbfawekjfhbaejkwfhbajkehfbk";
            String redirectUri = "http://localhost:3000";

            UserLogoutRequest userLogoutRequest = new UserLogoutRequest(accessToken, redirectUri);
            ResponseCookie logoutRequestCookie = ResponseCookie.from(COOKIE_NAME_USER_LOGOUT_REQUEST, CookieUtil.serialize(userLogoutRequest))
                    .path("/")
                    .domain(SERVER_DOMAIN)
                    .maxAge(60)
                    .httpOnly(true)
                    .secure(true)
                    .sameSite(org.springframework.boot.web.server.Cookie.SameSite.NONE.attributeValue())
                    .build();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/auth/logout")
                            .cookie(
                                    MockCookie.parse(logoutRequestCookie.toString())
                            )
            );

            // then
            perform.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.INVALID_ACCESS_TOKEN.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.INVALID_ACCESS_TOKEN.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }
}