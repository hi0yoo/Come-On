package com.comeon.userservice.web.user.query;

import com.amazonaws.services.s3.AmazonS3;
import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.file.UploadedFileInfo;
import com.comeon.userservice.web.user.response.UserDetailResponse;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import io.findify.s3mock.S3Mock;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserQueryServiceTest {

    @Autowired
    UserQueryService userQueryService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProfileImgRepository profileImgRepository;

    @Autowired
    EntityManager em;

    @Autowired
    FileManager fileManager;

    @Value("${profile.dirName}")
    String dirName;

    User user;
    ProfileImg profileImg;

    @AfterTestClass
    public void teardown(@Autowired S3Mock s3Mock,
                         @Autowired AmazonS3 amazonS3) {
        amazonS3.shutdown();
        s3Mock.stop();
        log.info("[teardown] ok");
    }

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
    @DisplayName("유저 상세 정보 조회")
    class getUserDetails {

        @Test
        @DisplayName("프로필 이미지가 있는, 등록된 유저의 식별값이 넘어오면 " +
                "userId, nickname, profileImgId, profileImgUrl, role, email, name 정보를 반환한다.")
        void whenUserHasProfileImg() throws IOException {
            // given
            initUser();
            initProfileImg();
            Long userId = user.getId();

            // when
            UserDetailResponse userDetails = userQueryService.getUserDetails(userId);

            // then
            String profileImgUrl = userDetails.getProfileImg() != null ? userDetails.getProfileImg().getImageUrl() : null;
            log.info("userProfileImgUrl : {}", profileImgUrl);
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUserId()).isEqualTo(userId);
            assertThat(userDetails.getNickname()).isEqualTo(user.getNickname());
            assertThat(userDetails.getEmail()).isEqualTo(user.getAccount().getEmail());
            assertThat(userDetails.getName()).isEqualTo(user.getAccount().getName());
            assertThat(userDetails.getRole()).isEqualTo(user.getRole().getRoleValue());
            assertThat(userDetails.getProfileImg()).isNotNull();
            assertThat(userDetails.getProfileImg().getId()).isEqualTo(profileImg.getId());
            assertThat(userDetails.getProfileImg().getImageUrl()).isNotNull();
        }

        @Test
        @DisplayName("프로필 이미지가 없는, 등록된 유저의 식별값이 넘어오면 " +
                "userId, nickname, role, email, name 정보를 반환한다.")
        void whenUserDoesNotHaveProfileImg() {
            // given
            initUser();
            Long userId = user.getId();

            // when
            UserDetailResponse userDetails = userQueryService.getUserDetails(userId);

            // then
            String profileImgUrl = userDetails.getProfileImg() != null ? userDetails.getProfileImg().getImageUrl() : null;
            log.info("userProfileImgUrl : {}", profileImgUrl);
            assertThat(userDetails).isNotNull();
            assertThat(userDetails.getUserId()).isEqualTo(userId);
            assertThat(userDetails.getNickname()).isEqualTo(user.getNickname());
            assertThat(userDetails.getEmail()).isEqualTo(user.getAccount().getEmail());
            assertThat(userDetails.getName()).isEqualTo(user.getAccount().getName());
            assertThat(userDetails.getRole()).isEqualTo(user.getRole().getRoleValue());
            assertThat(userDetails.getProfileImg()).isNull();
        }

        @Test
        @DisplayName("탈퇴한 유저를 조회하면, CustomException 발생한다. 예외는 ErrorCode.ALREADY_WITHDRAW 가진다.")
        void whenUserWithdrawn() {
            // given
            initUser();
            Long userId = user.getId();

            user.withdrawal();
            em.flush();

            // when, then
            assertThatThrownBy(
                    () -> userQueryService.getUserSimple(userId)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }

        @Test
        @DisplayName("등록되지 않은 유저 식발값이 넘어오면, EntityNotFoundException 발생한다.")
        void whenInvalidUserId() {
            // given
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> userQueryService.getUserSimple(invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("유저 기본 정보 조회")
    class getUserSimple {

        @Test
        @DisplayName("프로필 이미지가 있는, 등록된 유저의 식별값이 넘어오면 " +
                "userId, nickname, profileImgUrl 정보를 반환한다.")
        void whenUserHasProfileImg() throws IOException {
            // given
            initUser();
            initProfileImg();
            Long userId = user.getId();

            // when
            UserSimpleResponse userSimple = userQueryService.getUserSimple(userId);

            // then
            log.info("userProfileImgUrl : {}", userSimple.getProfileImgUrl());
            assertThat(userSimple).isNotNull();
            assertThat(userSimple.getUserId()).isEqualTo(userId);
            assertThat(userSimple.getNickname()).isEqualTo(user.getNickname());
            assertThat(userSimple.getProfileImgUrl()).isNotNull();
        }

        @Test
        @DisplayName("프로필 이미지가 없는, 등록된 유저의 식별값이 넘어오면 " +
                "userId, nickname 정보를 반환한다.")
        void whenUserDoesNotHaveProfileImg() {
            // given
            initUser();
            Long userId = user.getId();

            // when
            UserSimpleResponse userSimple = userQueryService.getUserSimple(userId);

            // then
            log.info("userProfileImgUrl : {}", userSimple.getProfileImgUrl());
            assertThat(userSimple).isNotNull();
            assertThat(userSimple.getUserId()).isEqualTo(userId);
            assertThat(userSimple.getNickname()).isEqualTo(user.getNickname());
            assertThat(userSimple.getProfileImgUrl()).isNull();
        }

        @Test
        @DisplayName("탈퇴한 유저를 조회하면, CustomException 발생한다. 예외는 ErrorCode.ALREADY_WITHDRAW 가진다.")
        void whenUserWithdrawn() {
            // given
            initUser();
            Long userId = user.getId();

            user.withdrawal();
            em.flush();

            // when, then
            assertThatThrownBy(
                    () -> userQueryService.getUserSimple(userId)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }

        @Test
        @DisplayName("등록되지 않은 유저 식발값이 넘어오면, EntityNotFoundException 발생한다.")
        void whenInvalidUserId() {
            // given
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> userQueryService.getUserSimple(invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

}