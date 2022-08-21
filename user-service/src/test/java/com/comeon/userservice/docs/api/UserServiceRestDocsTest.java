package com.comeon.userservice.docs.api;

import com.comeon.userservice.docs.config.RestDocsSupport;
import com.comeon.userservice.docs.utils.RestDocsUtil;
import com.comeon.userservice.domain.user.entity.Account;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
public class UserServiceRestDocsTest extends RestDocsSupport {

    @Nested
    @DisplayName("유저 정보 저장")
    class userSave {

        String oauthId;
        String provider;
        String email;
        String name;
        String profileImgUrl;

        Map<String, Object> generateRequestBody() {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("oauthId", oauthId);
            request.put("provider", provider);
            request.put("name", name);
            request.put("email", email);
            request.put("profileImgUrl", profileImgUrl);
            return request;
        }

        @Test
        @DisplayName("[docs] config - 지원하는 Provider 정보")
        void providers() throws Exception {
            ResultActions perform = mockMvc.perform(
                    get("/docs/providers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            Map<String, String> data = (Map<String, String>) objectMapper
                    .readValue(perform.andReturn()
                                    .getResponse()
                                    .getContentAsByteArray(),
                            new TypeReference<Map<String, Object>>() {}
                    )
                    .get("data");

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    RestDocsUtil.customResponseFields(
                                            "common-response", beneathPath("data").withSubsectionId("providers"),
                                            attributes(key("title").value("Provider 목록")),
                                            enumConvertFieldDescriptor(data)
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("[docs] success - 유저 정보 저장에 성공")
        void success() throws Exception {
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    RestDocsUtil.customRequestFields(
                                            "custom-request", null,
                                            attributes(key("title").value("요청 필드")),
                                            fieldWithPath("oauthId").type(JsonFieldType.STRING).description("OAuth 로그인 성공시, Provider에서 제공하는 유저 ID값"),
                                            fieldWithPath("provider").type(JsonFieldType.STRING).description("OAuth 유저 정보 제공자"),
                                            fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일 정보"),
                                            fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름 또는 닉네임 정보"),
                                            fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저 프로필 이미지 URL").optional()
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                            fieldWithPath("role").type(JsonFieldType.STRING).description("저장된 유저의 권한")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("[docs] fail - 지원하지 않는 Provider 지정한 경우")
        void failNotSupportedProvider() throws Exception {
            oauthId = "12345";
            provider = "daum".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isBadRequest())
                    .andDo(
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
        @DisplayName("[docs] fail - 요청 데이터 검증에 실패한 경우")
        void failValid() throws Exception {
            provider = "daum".toUpperCase();
            name = "testName1";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isBadRequest())
                    .andDo(
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

    @Nested
    @DisplayName("유저 정보 조회")
    class userDetails {

        @Autowired
        UserRepository userRepository;

        private User createAndSaveUser() {
            String oauthId = "12345";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName1";
            String email = "email1@email.com";
            String profileImgUrl = "profileImgUrl";

            User user = User.builder()
                    .account(
                            Account.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();

            return userRepository.save(user);
        }

        @Test
        @DisplayName("[docs] success - 해당 식별자를 가진 데이터가 존재할 경우")
        void success() throws Exception {
            User user = createAndSaveUser();

            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get("/users/{userId}", user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    pathParameters(parameterWithName("userId").description("유저 식별자")),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값"),
                                            fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임"),
                                            fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("[docs] fail - 해당 식별자를 가진 데이터가 존재하지 않을 경우")
        void fail() throws Exception {
            Long notExistUserId = 100L;
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get("/users/{userId}", notExistUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            perform.andExpect(status().isBadRequest())
                    .andDo(
                            restDocs.document(
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                    )
                            )
                    );
        }

        @Nested
        @DisplayName("유저 정보 조회")
        class myDetails {

            String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

            private User createAndSaveUser() {
                String oauthId = "12345";
                OAuthProvider provider = OAuthProvider.KAKAO;
                String name = "testName1";
                String email = "email1@email.com";
                String profileImgUrl = "profileImgUrl";

                User user = User.builder()
                        .account(
                                Account.builder()
                                        .oauthId(oauthId)
                                        .provider(provider)
                                        .email(email)
                                        .name(name)
                                        .profileImgUrl(profileImgUrl)
                                        .build()
                        )
                        .build();

                return userRepository.save(user);
            }

            @Test
            @DisplayName("[docs] success - 현재 로그인하여 사용중인 유저의 상세 정보 조회 성공")
            void success() throws Exception {
                User user = createAndSaveUser();

                String accessToken = Jwts.builder()
                        .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                        .claim("auth", user.getRole().getRoleValue())
                        .setIssuer("test")
                        .setIssuedAt(Date.from(Instant.now()))
                        .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                        .setSubject(user.getId().toString())
                        .compact();

                ResultActions perform = mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                );

                perform.andExpect(status().isOk())
                        .andDo(
                                restDocs.document(
                                        RestDocsUtil.optionalRequestHeaders(
                                                "optional-request",
                                                attributes(key("title").value("요청 헤더")),
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                        ),
                                        responseFields(
                                                beneathPath("data").withSubsectionId("data"),
                                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값 정보"),
                                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임 정보"),
                                                fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지 URL"),
                                                fieldWithPath("email").type(JsonFieldType.STRING).description("OAuth2 Provider로부터 제공받은 유저의 이메일 정보"),
                                                fieldWithPath("name").type(JsonFieldType.STRING).description("OAuth2 Provider로부터 제공받은 유저의 이름 정보")
                                        )
                                )
                        );
            }
        }

        @Nested
        @DisplayName("회원 탈퇴")
        class userWithdraw {

            String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

            private User createAndSaveUser() {
                String oauthId = "12345";
                OAuthProvider provider = OAuthProvider.KAKAO;
                String name = "testName1";
                String email = "email1@email.com";
                String profileImgUrl = "profileImgUrl";

                User user = User.builder()
                        .account(
                                Account.builder()
                                        .oauthId(oauthId)
                                        .provider(provider)
                                        .email(email)
                                        .name(name)
                                        .profileImgUrl(profileImgUrl)
                                        .build()
                        )
                        .build();

                return userRepository.save(user);
            }

            @Test
            @DisplayName("[docs] success - 회원 탈퇴에 성공하면 요청 성공 message를 응답 데이터로 담는다.")
            void success() throws Exception {
                User user = createAndSaveUser();

                String accessToken = Jwts.builder()
                        .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                        .claim("auth", user.getRole().getRoleValue())
                        .setIssuer("test")
                        .setIssuedAt(Date.from(Instant.now()))
                        .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                        .setSubject(user.getId().toString())
                        .compact();

                ResultActions perform = mockMvc.perform(
                        delete("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                );

                perform.andExpect(status().isOk())
                        .andDo(
                                restDocs.document(
                                        RestDocsUtil.optionalRequestHeaders(
                                                "optional-request",
                                                attributes(key("title").value("요청 헤더")),
                                                headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                        ),
                                        responseFields(
                                                beneathPath("data").withSubsectionId("data"),
                                                fieldWithPath("message").type(JsonFieldType.STRING).description("요청 성공 메세지")
                                        )
                                )
                        );
            }
        }
    }

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }
}
