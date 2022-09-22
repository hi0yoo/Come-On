package com.comeon.authservice.web.auth.controller;

import com.comeon.authservice.common.jwt.RedisRepository;
import com.comeon.authservice.common.exception.ErrorCode;
import com.comeon.authservice.common.jwt.JwtTokenInfo;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;

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

        @Nested
        @DisplayName("로그아웃")
        class logout {

            @Test
            @DisplayName("유효한 AccessToken이면 BlackList에 해당 AccessToken을 넣고, 사용자의 RefreshToken을 지운다.")
            void success() throws Exception {
                // given
                Long userId = 1L;
                String userRole = "ROLE_USER";
                JwtTokenInfo accessTokenInfo = generateAccessToken(userId, userRole, Instant.now(), Instant.now().plusSeconds(300));

                Instant refreshTokenIssuedAt = Instant.now();
                Instant refreshTokenExpiredAt = refreshTokenIssuedAt.plusSeconds(60 * 60 * 24 * 7 + 10);
                // refreshToken 세팅 - 로그인 됨
                JwtTokenInfo refreshTokenInfo = generateRefreshToken(refreshTokenIssuedAt, refreshTokenExpiredAt);
                redisRepository.addRefreshToken(
                        userId.toString(),
                        refreshTokenInfo.getValue(),
                        Duration.between(refreshTokenIssuedAt, refreshTokenExpiredAt)
                );

                // when
                String requestAccessToken = accessTokenInfo.getValue();
                ResultActions perform = mockMvc.perform(
                        post("/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + requestAccessToken)
                );

                // then
                perform.andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.message").isNotEmpty());

                assertThat(redisRepository.findBlackList(requestAccessToken)).isPresent();
                assertThat(redisRepository.findRefreshTokenByUserId(userId.toString())).isNotPresent();

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
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("로그아웃 성공 메시지")
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
                        post("/auth/logout")
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
                        post("/auth/logout")
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
                        post("/auth/logout")
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
                        post("/auth/logout")
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
                        post("/auth/logout")
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
                        post("/auth/logout")
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
                                        fieldWithPath("expiry").type(JsonFieldType.NUMBER).description("재발급된 Access Token의 만료일 - UNIX TIME")
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
                                        fieldWithPath("expiry").type(JsonFieldType.NUMBER).description("재발급된 Access Token의 만료일 - UNIX TIME")
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
}