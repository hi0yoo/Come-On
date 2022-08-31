package com.comeon.userservice.web.profileimage.query;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.config.S3MockConfig;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@Import({S3MockConfig.class})
@SpringBootTest
class ProfileImgQueryServiceTest {

    @Autowired
    ProfileImgQueryService profileImgQueryService;

    @Autowired
    ProfileImgRepository profileImgRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    User user;
    ProfileImg profileImg;

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
    void initProfileImg() {
        profileImg = profileImgRepository.save(
                ProfileImg.builder()
                        .user(user)
                        .originalName("originalName")
                        .storedName("storedName")
                        .build()
        );
    }

    @Nested
    @DisplayName("유저 식별자로 저장된 프로필 이미지 이름 조회")
    class getStoredFileNameByUserId {

        @Test
        @DisplayName("유저가 등록한 프로필 이미지가 있으면, 이미지의 storedName을 반환한다.")
        void whenUserHasProfileImg() {
            // given
            initUser();
            initProfileImg();

            // when
            String storedFileName = profileImgQueryService.getStoredFileNameByUserId(user.getId());

            // then
            assertThat(storedFileName).isNotNull();
            assertThat(storedFileName).isEqualTo(profileImg.getStoredName());
        }

        @Test
        @DisplayName("유저가 등록한 프로필 이미지가 없으면, null을 반환한다.")
        void whenUserHasNotProfileImg() {
            // given
            initUser();

            // when
            String storedFileName = profileImgQueryService.getStoredFileNameByUserId(user.getId());

            // then
            assertThat(storedFileName).isNull();
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId와 일치하는 유저가 없으면 null을 반환한다.")
        void whenNotMatchUser() {
            // given
            Long invalidUserId = 100L;

            // when
            String storedFileName = profileImgQueryService.getStoredFileNameByUserId(invalidUserId);

            // then
            assertThat(storedFileName).isNull();
        }

        @Test
        @DisplayName("조회한 유저가 탈퇴한 회원이면 CustomException을 발생시킨다. 예외 내부 ErrorCode 는 ALREADY_WITHDRAW 이다.")
        void whenUserWasWithDrawn() {
            // given
            initUser();
            initProfileImg();
            user.withdrawal();
            em.flush();

            // when
            assertThatThrownBy(
                    () -> profileImgQueryService.getStoredFileNameByUserId(user.getId())
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 식별자와 현재 유저 식별자로 저장된 프로필 이미지 이름 조회")
    class getStoredFileNameByProfileImgIdAndUserId {

        @Test
        @DisplayName("프로필 이미지를 등록한 유저의 식별자와 파라미터로 넘어온 userId가 같으면 해당 프로필 이미지의 storedName을 반환한다.")
        void success() {
            // given
            initUser();
            initProfileImg();

            Long userId = user.getId();
            Long profileImgId = profileImg.getId();

            // when
            String storedFileName = profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(profileImgId, userId);

            // then
            assertThat(storedFileName).isNotNull();
            assertThat(storedFileName).isEqualTo(profileImg.getStoredName());
        }

        @Test
        @DisplayName("프로필 이미지를 등록한 유저 식별자와 파라미터로 넘어온 userId가 다르면, CustomException 발생한다. ErrorCode.No_AUTHORITIES를 가진다.")
        void failNoAuthorities() {
            // given
            initUser();
            initProfileImg();

            Long InvalidUserId = 100L;
            Long profileImgId = profileImg.getId();

            // when, then
            assertThatThrownBy(
                    () -> profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(profileImgId, InvalidUserId)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("파라미터로 넘어온 프로필 이미지의 식별자와 일치하는 엔티티가 없으면 EntityNotFoundException 발생한다.")
        void failEntityNotFound() {
            // given
            initUser();
            Long userId = user.getId();
            Long invalidProfileImgId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(invalidProfileImgId, userId)
            )
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("조회한 유저가 탈퇴한 회원이면 CustomException을 발생시킨다. 예외 내부 ErrorCode 는 ALREADY_WITHDRAW 이다.")
        void whenUserWasWithdrawn() {
            // given
            initUser();
            initProfileImg();
            user.withdrawal();
            em.flush();

            // when
            assertThatThrownBy(
                    () -> profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(profileImg.getId(), user.getId())
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }
    }

}