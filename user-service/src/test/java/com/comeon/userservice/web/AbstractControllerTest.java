package com.comeon.userservice.web;

import com.comeon.userservice.config.S3MockConfig;
import com.comeon.userservice.docs.config.RestDocsConfig;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.S3FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;

@Import({
        RestDocsConfig.class,
        S3FileManager.class,
        S3MockConfig.class
})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractControllerTest {

    protected static final String BEARER_TOKEN_TYPE = "Bearer ";

    @Value("${s3.folder-name.user}")
    protected String dirName;

    @SpyBean
    protected FileManager fileManager;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    protected String jwtSecretKey;

    protected String generateUserAccessToken(Long userId) {
        String userRole = "ROLE_USER";
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", userRole)
                .setIssuer("test")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                .setSubject(userId.toString())
                .compact();
    }

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    private LocalDateTime randomDate() {
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        return now.minusDays(random.nextInt(10));
    }

    public MockMultipartFile getMockMultipartFile(String fileNameWithExt) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "imgFile",
                fileNameWithExt,
                ContentType.IMAGE_JPEG.getMimeType(),
                new FileInputStream(
                        ResourceUtils.getFile(this.getClass().getResource("/static/" + fileNameWithExt))
                )
        );
        return mockMultipartFile;
    }

    private UploadedFileInfo fileUpload() {
        MockMultipartFile mockMultipartFile = null;
        try {
            mockMultipartFile = getMockMultipartFile("test-img.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UploadedFileInfo uploadedFileInfo = fileManager.upload(mockMultipartFile, dirName);
        return uploadedFileInfo;
    }

    private Long userIdGenerator = 1L;
    private Long userProfileImgIdGenerator = 1L;
    private Long userAccountIdGenerator = 1L;
    private Long userOauthIdGenerator = 10000L;

    public User setUser(String oauthId, String providerName, String name, String email, String profileImgUrl) {
        Long userAccountId = userAccountIdGenerator++;
        Long userId = userIdGenerator++;
        UserAccount userAccount = UserAccount.builder()
                .oauthId(oauthId)
                .provider(OAuthProvider.valueOf(providerName))
                .name(name)
                .email(email)
                .profileImgUrl(profileImgUrl) // TODO 곧 제거
                .build();

        ReflectionTestUtils.setField(userAccount, "id", userAccountId);
        LocalDateTime accountCreatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(userAccount, "createdDate", accountCreatedAt);
        ReflectionTestUtils.setField(userAccount, "lastModifiedDate", accountCreatedAt);

        User user = User.builder()
                .account(userAccount)
                .build();

        ReflectionTestUtils.setField(user, "id", userId);
        LocalDateTime userCreatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(user, "createdDate", userCreatedAt);
        ReflectionTestUtils.setField(user, "lastModifiedDate", userCreatedAt);

        return user;
    }

    public User setUser() {
        long userAccountId = userAccountIdGenerator++;
        long userId = userIdGenerator++;
        UserAccount userAccount = UserAccount.builder()
                .oauthId(String.valueOf(userOauthIdGenerator++))
                .provider(OAuthProvider.KAKAO)
                .name("userName" + userAccountId)
                .email("email" + userAccountId + "@email.com")
                .profileImgUrl("profileImgUrl") // TODO 곧 제거
                .build();

        ReflectionTestUtils.setField(userAccount, "id", userAccountId);
        LocalDateTime accountCreatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(userAccount, "createdDate", accountCreatedAt);
        ReflectionTestUtils.setField(userAccount, "lastModifiedDate", accountCreatedAt);

        User user = User.builder()
                .account(userAccount)
                .build();

        ReflectionTestUtils.setField(user, "id", userId);
        LocalDateTime userCreatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(user, "createdDate", userCreatedAt);
        ReflectionTestUtils.setField(user, "lastModifiedDate", userCreatedAt);

        return user;
    }

    public ProfileImg setProfileImg(User user) {
        UploadedFileInfo uploadedFileInfo = fileUpload();
        ProfileImg profileImg = ProfileImg.builder()
                .user(user)
                .originalName(uploadedFileInfo.getOriginalFileName())
                .storedName(uploadedFileInfo.getStoredFileName())
                .build();

        ReflectionTestUtils.setField(profileImg, "id", userProfileImgIdGenerator++);
        LocalDateTime profileImgCreatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(profileImg, "createdDate", profileImgCreatedAt);
        ReflectionTestUtils.setField(profileImg, "lastModifiedDate", profileImgCreatedAt);

        return profileImg;
    }
}
