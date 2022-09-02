package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.config.S3MockConfig;
import com.comeon.userservice.config.argresolver.JwtArgumentResolver;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.web.feign.authservice.AuthServiceFeignClient;
import com.comeon.userservice.web.feign.authservice.response.LogoutSuccessResponse;
import com.comeon.userservice.web.common.exception.resolver.CommonControllerAdvice;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.user.request.UserModifyRequest;
import com.comeon.userservice.web.user.response.UserWithdrawResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Transactional
@ActiveProfiles("test")
@Import({S3MockConfig.class})
@SpringBootTest
class UserControllerTest {

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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtArgumentResolver jwtArgumentResolver;

    @MockBean
    AuthServiceFeignClient authServiceFeignClient;

    @Autowired
    UserController userController;

    MockMvc mockMvc;

    @BeforeEach
    void initMockMvc(final WebApplicationContext context) throws ServletException {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new CommonControllerAdvice())
                .setCustomArgumentResolvers(jwtArgumentResolver)
                .build();
    }

    User user;
    void initUser() {
        user = userRepository.save(
                User.builder()
                        .account(
                                UserAccount.builder()
                                        .oauthId("oauthId")
                                        .provider(OAuthProvider.KAKAO)
                                        .email("email")
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
    @DisplayName("회원 등록")
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
        @DisplayName("success - 요청 데이터 검증에 성공하고, 존재하지 않는 유저라면, 회원 정보를 새로 등록하고, " +
                "userId, nickname, email, name, role을 응답으로 내린다.")
        void userSave_success_1() throws Exception {
            // given
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            // when
            Map<String, Object> requestBody = generateRequestBody();
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            User findUser = userRepository.findByOAuthIdAndProvider(
                    oauthId,
                    OAuthProvider.valueOf(provider)
            ).orElseThrow();

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(findUser.getId()))

                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(findUser.getNickname()))

                    .andExpect(jsonPath("$.data.role").exists())
                    .andExpect(jsonPath("$.data.role").isNotEmpty())
                    .andExpect(jsonPath("$.data.role").value(findUser.getRole().getRoleValue()))

                    .andExpect(jsonPath("$.data.email").exists())
                    .andExpect(jsonPath("$.data.email").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value(findUser.getAccount().getEmail()))

                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.name").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value(findUser.getAccount().getName()))

                    .andExpect(jsonPath("$.data.profileImg").doesNotExist());
        }

        @Test
        @DisplayName("success - 요청 데이터 검증에 성공하고, 기존에 존재하는 유저라면, 변경된 정보로 수정하고, " +
                "userId, nickname, email, name, role, profileImg를 응답으로 내린다. " +
                "profileImg는 등록이 안되었다면 null 일 수 있다.")
        void userSave_success_2() throws Exception {
            // given
            initUser();
            oauthId = user.getAccount().getOauthId();
            provider = user.getAccount().getProvider().name();
            name = "이름수정";
            email = "changed@email.com";
            profileImgUrl = "profileImgUrl";

            // when
            Map<String, Object> requestBody = generateRequestBody();
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            User findUser = userRepository.findByOAuthIdAndProvider(
                    oauthId,
                    OAuthProvider.valueOf(provider)
            ).orElseThrow();

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(findUser.getId()))

                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(findUser.getNickname()))

                    .andExpect(jsonPath("$.data.role").exists())
                    .andExpect(jsonPath("$.data.role").isNotEmpty())
                    .andExpect(jsonPath("$.data.role").value(findUser.getRole().getRoleValue()))

                    .andExpect(jsonPath("$.data.email").exists())
                    .andExpect(jsonPath("$.data.email").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value(findUser.getAccount().getEmail()))

                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.name").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value(findUser.getAccount().getName()))

                    .andExpect(jsonPath("$.data.profileImg").doesNotExist());
        }

        @Test
        @DisplayName("fail - email이 유효한 형식이 아니면 검증이 실패하여 http status 400 오류를 반환한다.")
        void userSave_fail_1() throws Exception {
            // given
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1";

            // when
            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - 등록되지 않은 Provider가 요청으로 넘어오면, 검증이 실패하고 http status 400 오류를 반환한다.")
        void userSave_fail_2() throws Exception {
            oauthId = "12345";
            provider = "daum".toUpperCase();
            name = "testName1";
            email = "email1";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - oauthId, provider, email, name은 필수값으로, 입력하지 않으면 http status 400 오류를 반환한다.")
        void userSave_fail_3() throws Exception {
            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("유저 정보 조회")
    class userDetails {

        @Test
        @DisplayName("success - 프로필 이미지가 있는, 존재하는 유저를 검색하면 해당 유저의 id, nickname, profileImgUrl 정보를 출력한다.")
        void userDetailSuccess() throws Exception {
            // given
            initUser();
            initProfileImg();
            Long userId = user.getId();

            // when
            ResultActions perform = mockMvc.perform(
                    get("/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))
                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                    .andExpect(jsonPath("$.data.profileImgUrl").exists())
                    .andExpect(jsonPath("$.data.profileImgUrl").isNotEmpty())
                    // email과 name 필드는 없다.
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.name").doesNotExist());
        }

        @Test
        @DisplayName("success - 유저가 profileImgUrl 정보를 갖고있지 않으면, profileImgUrl 필드는 null 일 수 있다.")
        void userDetailSuccessNoProfileImgUrl() throws Exception {
            // given
            initUser();
            Long userId = user.getId();

            // when
            ResultActions perform = mockMvc.perform(
                    get("/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                    .andExpect(jsonPath("$.data.profileImgUrl").isEmpty())
                    // email과 name 필드는 없다.
                    .andExpect(jsonPath("$.data.email").doesNotExist())
                    .andExpect(jsonPath("$.data.name").doesNotExist());
        }

        @Test
        @DisplayName("fail - 존재하지 않는 유저를 검색하면 요청이 실패하고 http status 400 반환한다.")
        void userProfileSuccessNoProfileImgUrl() throws Exception {
            Long userId = 100L;

            ResultActions perform = mockMvc.perform(
                    get("/users/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            perform.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("내 상세정보 조회")
    class myDetails {

        @Test
        @DisplayName("success - 요청한 유저가 프로필 이미지가 있으면, " +
                "userId, nickname, email, name, role, profileImg 정보를 반환한다.")
        void success() throws Exception {
            // given
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
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))

                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))

                    .andExpect(jsonPath("$.data.role").exists())
                    .andExpect(jsonPath("$.data.role").isNotEmpty())
                    .andExpect(jsonPath("$.data.role").value(user.getRole().getRoleValue()))

                    .andExpect(jsonPath("$.data.email").exists())
                    .andExpect(jsonPath("$.data.email").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value(user.getAccount().getEmail()))

                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.name").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value(user.getAccount().getName()))

                    .andExpect(jsonPath("$.data.profileImg").exists())
                    .andExpect(jsonPath("$.data.profileImg").isNotEmpty())
                    .andExpect(jsonPath("$.data.profileImg.id").exists())
                    .andExpect(jsonPath("$.data.profileImg.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.profileImg.imageUrl").exists())
                    .andExpect(jsonPath("$.data.profileImg.imageUrl").isNotEmpty());
        }

        @Test
        @DisplayName("success - 요청한 유저가 프로필 이미지가 없으면, " +
                "userId, nickname, email, name, role 정보를 반환한다.")
        void success2() throws Exception {
            initUser();

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
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))

                    .andExpect(jsonPath("$.data.nickname").exists())
                    .andExpect(jsonPath("$.data.nickname").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))

                    .andExpect(jsonPath("$.data.role").exists())
                    .andExpect(jsonPath("$.data.role").isNotEmpty())
                    .andExpect(jsonPath("$.data.role").value(user.getRole().getRoleValue()))

                    .andExpect(jsonPath("$.data.email").exists())
                    .andExpect(jsonPath("$.data.email").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value(user.getAccount().getEmail()))

                    .andExpect(jsonPath("$.data.name").exists())
                    .andExpect(jsonPath("$.data.name").isNotEmpty())
                    .andExpect(jsonPath("$.data.name").value(user.getAccount().getName()))

                    .andExpect(jsonPath("$.data.profileImg").doesNotExist());
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class userWithdraw {

        String successMessage = UserWithdrawResponse.SUCCESS_MESSAGE;

        private void setAuthServiceLogoutStub(String accessToken) {
            given(authServiceFeignClient.logout(accessToken))
                    .willReturn(
                            ApiResponse.createSuccess(new LogoutSuccessResponse("로그아웃이 성공적으로 완료되었습니다."))
                    );
        }

        @Test
        @DisplayName("succss - 회원 탈퇴 요청을 성공적으로 처리하면, 요청 성공 처리 메시지를 반환한다.")
        void success() throws Exception {
            // given
            initUser();

            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", user.getRole().getRoleValue())
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(user.getId().toString())
                    .compact();

            setAuthServiceLogoutStub(accessToken);

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value(successMessage));
        }

        @Test
        @DisplayName("success - 회원 탈퇴 요청을 성공적으로 처리하면, 해당 회원을 더 이상 조회할 수 없다. http staus 400 반환한다.")
        void afterSuccess() throws Exception {
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

            setAuthServiceLogoutStub(accessToken);

            // when
            mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            ).andExpect(status().isOk());

            // then
            mockMvc.perform(
                    get("/users/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            ).andExpect(status().isBadRequest());

            // 기존 AccessToken 은 사용 불가 처리되어 기존 AccessToken이 넘어올 일은 없다만..
            mockMvc.perform(
                    get("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            ).andExpect(status().isBadRequest());

            mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            ).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("유저 정보 수정")
    class userModify {

        @Test
        @DisplayName("success - 유저 닉네임 변경에 성공하면 http status 200 반환한다.")
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

            String newNickname = "newNickname";
            UserModifyRequest request = new UserModifyRequest(newNickname);

            // when
            mockMvc.perform(
                    patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isOk());
        }

        @Test
        @DisplayName("fail - 변경할 닉네임이 빈 문자열이면, 요청에 실패하고 http status 400 반환한다.")
        void fail_1() throws Exception {
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

            String newNickname = "";
            UserModifyRequest request = new UserModifyRequest(newNickname);

            // when
            mockMvc.perform(
                    patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - 변경할 닉네임이 null이면, 요청에 실패하고 http status 400 반환한다.")
        void fail_2() throws Exception {
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

            UserModifyRequest request = new UserModifyRequest(null);

            // when
            mockMvc.perform(
                    patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isBadRequest());
        }
    }
}