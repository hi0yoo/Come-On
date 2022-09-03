package com.comeon.userservice.docs.api;

import com.comeon.userservice.config.S3MockConfig;
import com.comeon.userservice.docs.config.RestDocsSupport;
import com.comeon.userservice.docs.utils.RestDocsUtil;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.feign.authservice.AuthServiceFeignClient;
import com.comeon.userservice.web.feign.authservice.response.LogoutSuccessResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@ActiveProfiles("test")
@Import({S3MockConfig.class})
@SpringBootTest
public class UserServiceRestDocsTest extends RestDocsSupport {

    @Value("${s3.folder-name.user}")
    String dirName;

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImgRepository profileImgRepository;

    @Autowired
    FileManager fileManager;

    @MockBean
    AuthServiceFeignClient authServiceFeignClient;

    User user;

    void initUser() {
        user = userRepository.save(
                User.builder()
                        .account(
                                UserAccount.builder()
                                        .oauthId("12345")
                                        .provider(OAuthProvider.KAKAO)
                                        .email("email1@email.com")
                                        .name("name")
                                        .build()
                        )
                        .build()
        );
    }

    ProfileImg profileImg;

    void initProfileImg() throws IOException {
        File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.png"));
        UploadedFileInfo uploadedFileInfo = fileManager.upload(getMockMultipartFile(imgFile), dirName);
        profileImg = profileImgRepository.save(
                ProfileImg.builder()
                        .user(user)
                        .originalName(uploadedFileInfo.getOriginalFileName())
                        .storedName(uploadedFileInfo.getStoredFileName())
                        .build()
        );
    }

    private MockMultipartFile getMockMultipartFile(File imgFile) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "imgFile",
                "test-img.png",
                ContentType.IMAGE_JPEG.getMimeType(),
                new FileInputStream(imgFile)
        );
        return mockMultipartFile;
    }

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
                            new TypeReference<Map<String, Object>>() {
                            }
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
            initUser();
            initProfileImg();

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
                                    requestFields(
                                            attributes(key("title").value("요청 필드")),
                                            fieldWithPath("oauthId").type(JsonFieldType.STRING).description("OAuth 로그인 성공시, Provider에서 제공하는 유저 ID값"),
                                            fieldWithPath("provider").type(JsonFieldType.STRING).description("OAuth 유저 정보 제공자"),
                                            fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일 정보"),
                                            fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름 또는 닉네임 정보"),
                                            fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저 프로필 이미지 URL").optional()
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                            fieldWithPath("nickname").type(JsonFieldType.STRING).description("저장된 유저의 닉네임"),
                                            fieldWithPath("name").type(JsonFieldType.STRING).description("저장된 유저의 이름 정보"),
                                            fieldWithPath("email").type(JsonFieldType.STRING).description("저장된 유저의 소셜 이메일"),
                                            fieldWithPath("role").type(JsonFieldType.STRING).description("저장된 유저의 권한"),
                                            fieldWithPath("profileImg.id").type(JsonFieldType.NUMBER).description("프로필 이미지의 식별값"),
                                            fieldWithPath("profileImg.imageUrl").type(JsonFieldType.STRING).description("프로필 이미지의 URL")
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
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지"),
                                            fieldWithPath("message.provider").type(JsonFieldType.ARRAY).description("API 오류 메시지")
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
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지"),
                                            fieldWithPath("message.provider").type(JsonFieldType.ARRAY).description("필드 오류 메시지"),
                                            fieldWithPath("message.oauthId").type(JsonFieldType.ARRAY).description("필드 오류 메시지"),
                                            fieldWithPath("message.email").type(JsonFieldType.ARRAY).description("필드 오류 메시지")
                                    )
                            )
                    );
        }
    }

    @Nested
    @DisplayName("유저 정보 조회")
    class userDetails {

        @Test
        @DisplayName("[docs] success - 해당 식별자를 가진 데이터가 존재할 경우")
        void success() throws Exception {
            initUser();
            initProfileImg();

            String path = "/users/{userId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, user.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    pathParameters(
                                            attributes(key("title").value(path)),
                                            parameterWithName("userId").description("유저 식별자")
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값"),
                                            fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임"),
                                            fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지"),
                                            fieldWithPath("status").type(JsonFieldType.STRING).description("유저 상태")
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
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                    )
                            )
                    );
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class myDetails {

        @Test
        @DisplayName("[docs] success - 현재 로그인하여 사용중인 유저의 상세 정보 조회 성공")
        void success() throws Exception {
            initUser();
            initProfileImg();

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
                                    requestHeaders(
                                            attributes(key("title").value("요청 헤더")),
                                            headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("현재 유저의 식별값"),
                                            fieldWithPath("nickname").type(JsonFieldType.STRING).description("현재 유저의 닉네임"),
                                            fieldWithPath("name").type(JsonFieldType.STRING).description("현재 유저의 이름 정보"),
                                            fieldWithPath("email").type(JsonFieldType.STRING).description("현재 유저의 소셜 이메일"),
                                            fieldWithPath("role").type(JsonFieldType.STRING).description("현재 유저의 권한"),
                                            fieldWithPath("profileImg.id").type(JsonFieldType.NUMBER).description("프로필 이미지의 식별값"),
                                            fieldWithPath("profileImg.imageUrl").type(JsonFieldType.STRING).description("프로필 이미지의 URL")
                                    )
                            )
                    );
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class userWithdraw {

        private User createAndSaveUser() {
            String oauthId = "12345";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName1";
            String email = "email1@email.com";
            String profileImgUrl = "profileImgUrl";

            User user = User.builder()
                    .account(
                            UserAccount.builder()
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

        private void setAuthServiceLogoutStub(String accessToken) {
            given(authServiceFeignClient.logout(accessToken))
                    .willReturn(
                            ApiResponse.createSuccess(new LogoutSuccessResponse("로그아웃이 성공적으로 완료되었습니다."))
                    );
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

            setAuthServiceLogoutStub(accessToken);

            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    requestHeaders(
                                            attributes(key("title").value("요청 헤더")),
                                            headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("요청 성공 메세지")
                                    )
                            )
                    );
        }
    }

    @Nested
    @DisplayName("프로필 이미지 저장")
    class profileImageSave {

        @Test
        @DisplayName("[docs] success - 유저 프로필 이미지 등록에 성공한 경우")
        void success() throws Exception {
            initUser();
            Long userId = user.getId();

            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", user.getRole().getRoleValue())
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.png"));
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imgFile",
                    "test-img.png",
                    ContentType.IMAGE_JPEG.getMimeType(),
                    new FileInputStream(imgFile)
            );

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/profile-image")
                            .file(mockMultipartFile)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    requestHeaders(
                                            attributes(key("title").value("요청 헤더")),
                                            headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                    ),
                                    requestParts(
                                            attributes(key("title").value("요청 파트")),
                                            partWithName("imgFile").description("등록할 프로필 이미지 파일")
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("profileImgId").type(JsonFieldType.NUMBER).description("저장된 프로필 이미지 식별값"),
                                            fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("저장된 프로필 이미지 URL")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("[docs] 프로필 이미지를 지정하지 않은 경우")
        void fail() throws Exception {
            // given
            initUser();
            Long userId = user.getId();

            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", user.getRole().getRoleValue())
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/profile-image")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            perform.andExpect(status().isBadRequest())
                    .andDo(
                            restDocs.document(
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지"),
                                            fieldWithPath("message.imgFile").type(JsonFieldType.ARRAY).description("API 오류 메시지")
                                    )
                            )
                    );
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제")
    class profileImageRemove {

        @Test
        @DisplayName("[docs] success - 프로필 이미지 삭제에 성공한 경우")
        void success() throws Exception {
            // given
            initUser();
            Long userId = user.getId();

            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", user.getRole().getRoleValue())
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            initProfileImg();

            // when
            String path = "/profile-image/{profileImgId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, profileImg.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    requestHeaders(
                                            attributes(key("title").value("요청 헤더")),
                                            headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                                    ),
                                    pathParameters(
                                            attributes(key("title").value(path)),
                                            parameterWithName("profileImgId").description("프로필 이미지 식별값")
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            attributes(key("title").value("응답 필드")),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("요청 성공 메시지")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("[docs] fail - 다른 사용자의 프로필을 삭제하려 하는 경우")
        void fail() throws Exception {
            // given
            initUser();
            Long invalidUserId = 100L;

            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", user.getRole().getRoleValue())
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(invalidUserId.toString())
                    .compact();

            initProfileImg();
            Long profileImgId = profileImg.getId();

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/profile-image/{profileImgId}", profileImgId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            perform.andExpect(status().isForbidden())
                    .andDo(
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

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }
}
