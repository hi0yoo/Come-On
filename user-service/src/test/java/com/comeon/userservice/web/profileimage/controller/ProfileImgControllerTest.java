package com.comeon.userservice.web.profileimage.controller;

import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.config.S3MockConfig;
import com.comeon.userservice.config.argresolver.JwtArgumentResolver;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.web.common.exception.resolver.CommonControllerAdvice;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@ActiveProfiles("test")
@Import({S3MockConfig.class})
@SpringBootTest
class ProfileImgControllerTest {

    @Value("${profile.dirName}")
    String dirName;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImgRepository profileImgRepository;

    @Autowired
    FileManager fileManager;

    @Autowired
    ProfileImgController profileImgController;

    @Autowired
    JwtArgumentResolver jwtArgumentResolver;

    MockMvc mockMvc;

    @BeforeEach
    void initMockMvc(final WebApplicationContext context) throws ServletException {
        mockMvc = MockMvcBuilders.standaloneSetup(profileImgController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new CommonControllerAdvice())
                .setCustomArgumentResolvers(jwtArgumentResolver)
                .build();
    }

    String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

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
        File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.jpeg"));
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
                "test-img.jpeg",
                ContentType.IMAGE_JPEG.getMimeType(),
                new FileInputStream(imgFile)
        );
        return mockMultipartFile;
    }

    @Nested
    @DisplayName("유저 프로필 이미지 수정")
    class profileImageSave {

        @Test
        @DisplayName("success - 이미지 파일이 요청 파라미터로 넘어오면 해당 이미지를 저장하고, 저장된 이미지의 url을 반환한다.")
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

            File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.jpeg"));
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imgFile",
                    "test-img.jpeg",
                    ContentType.IMAGE_JPEG.getMimeType(),
                    new FileInputStream(imgFile)
            );

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/profile-image")
                            .file(mockMultipartFile)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            User findUser = userRepository.findById(userId).orElseThrow();
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.profileImgId").exists())
                    .andExpect(jsonPath("$.data.profileImgId").isNotEmpty())
                    .andExpect(jsonPath("$.data.profileImgId").value(findUser.getProfileImg().getId()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.imageUrl").isNotEmpty());
        }

        @Test
        @DisplayName("fail - 요청 파라미터로 넘어온 파일이 없으면, 요청이 실패하고 http status 400 반환한다.")
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
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()));
        }
    }

    @Nested
    @DisplayName("유저 프로필 이미지 삭제")
    class userImageRemove {

        @Test
        @DisplayName("success - Path 변수로 넘어온 식별자를 갖는 프로필 이미지가 존재하고, " +
                "요청한 유저가 해당 프로필 등록자이면, " +
                "이를 삭제하고 http status 200 반환한다.")
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
            ResultActions perform = mockMvc.perform(
                    delete("/profile-image/{profileImgId}", profileImg.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").exists())
                    .andExpect(jsonPath("$.data.message").isNotEmpty());
        }

        @Test
        @DisplayName("fail - Path 변수로 넘어온 식별자를 갖는 프로필 이미지가 존재하지 않으면, " +
                "요청이 실패하고 http status 400 반환한다.")
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

            Long invalidProfileImgId = 100L;

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/profile-image/{profileImgId}", invalidProfileImgId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - Path 변수로 넘어온 식별자를 갖는 프로필 이미지가 존재하지만, " +
                "요청한 유저가 해당 프로필 등록자가 아니면, " +
                "요청이 실패하고 http status 403 반환한다.")
        void fail_2() throws Exception {
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

            // then
            perform.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("fail - Path 변수로 넘어온 식별자를 갖는 프로필 이미지가 존재하고 " +
                "요청한 유저가 해당 프로필 등록자이지만 탈퇴한 회원이라면, " +
                "요청이 실패하고 http status 400 반환한다.")
        void fail_3() throws Exception {
            // given
            initUser();
            user.withdrawal();
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

            // then
            perform.andExpect(status().isBadRequest());
        }
    }
}