package com.comeon.userservice.domain.profileimage.service;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.profileimage.repository.ProfileImgRepository;
import com.comeon.userservice.domain.profileimage.service.dto.ProfileImgDto;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProfileImgServiceTest {

    @Autowired
    ProfileImgService profileImgService;

    @Autowired
    ProfileImgRepository profileImgRepository;

    @Autowired
    EntityManager em;

    User user;

    @Autowired
    UserRepository userRepository;

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

    @Nested
    @DisplayName("프로필 이미지 저장")
    class saveProfileImg {

        @Test
        @DisplayName("유저의 프로필 이미지가 없으면 새로 저장하고 저장된 프로필 이미지의 식별값을 반환한다.")
        void saveNewProfileImg() {
            // given
            initUser();
            em.flush();

            ProfileImgDto profileImgDto = ProfileImgDto.builder()
                    .originalName("originalName")
                    .storedName("storedName")
                    .build();

            // when
            Long profileImgId = profileImgService.saveProfileImg(profileImgDto, user.getId());

            // then
            ProfileImg profileImg = profileImgRepository.findById(profileImgId).orElseThrow();

            assertThat(profileImg).isNotNull();
            assertThat(profileImg.getId()).isEqualTo(profileImgId);
            assertThat(profileImg.getOriginalName()).isEqualTo(profileImgDto.getOriginalName());
            assertThat(profileImg.getStoredName()).isEqualTo(profileImgDto.getStoredName());
            assertThat(profileImg.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("유저의 프로필 이미지가 있으면 기존 프로필 이미지의 정보를 변경하고, 해당 프로필 이미지의 식별값을 반환한다.")
        void updateOriginalProfileImg() {
            // given
            initUser();
            ProfileImg originalProfileImg = ProfileImg.builder()
                    .user(user)
                    .originalName("originalName")
                    .storedName("storedName")
                    .build();
            profileImgRepository.save(originalProfileImg);
            em.flush();

            ProfileImgDto profileImgDto = ProfileImgDto.builder()
                    .originalName("updatedOriginalName")
                    .storedName("updatedStoredName")
                    .build();

            // when
            Long profileImgId = profileImgService.saveProfileImg(profileImgDto, user.getId());

            // then
            ProfileImg updatedProfileImg = profileImgRepository.findById(profileImgId).orElseThrow();

            assertThat(updatedProfileImg).isNotNull();
            // 기존 파일 id와 같다.
            assertThat(updatedProfileImg.getId()).isEqualTo(originalProfileImg.getId());
            // 파일 정보가 수정된다.
            assertThat(updatedProfileImg.getId()).isEqualTo(profileImgId);
            assertThat(updatedProfileImg.getOriginalName()).isEqualTo(profileImgDto.getOriginalName());
            assertThat(updatedProfileImg.getStoredName()).isEqualTo(profileImgDto.getStoredName());
            assertThat(updatedProfileImg.getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("존재하지 않는 유저 식별자가 들어오면 EntityNotFoundException 발생한다.")
        void notExistUserId() {
            // given
            ProfileImgDto profileImgDto = ProfileImgDto.builder()
                    .originalName("updatedOriginalName")
                    .storedName("updatedStoredName")
                    .build();
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> profileImgService.saveProfileImg(profileImgDto, invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제")
    class removeProfileImg {

        @Test
        @DisplayName("프로필 이미지가 존재하면 해당 식별자로 프로필 이미지를 삭제한다.")
        void successRemoveProfileImg() {
            // given
            initUser();
            ProfileImg originalProfileImg = ProfileImg.builder()
                    .user(user)
                    .originalName("originalName")
                    .storedName("storedName")
                    .build();
            profileImgRepository.save(originalProfileImg);
            em.flush();

            Long profileImgId = originalProfileImg.getId();

            // when
            profileImgService.removeProfileImg(profileImgId);
            em.flush();

            // then
            assertThat(profileImgRepository.findById(profileImgId)).isNotPresent();
            assertThat(user.getProfileImg()).isNull();
            // 유저를 다시 조회해도 프로필 이미지는 null 이어야 한다.
            assertThat(userRepository.findById(user.getId()).orElseThrow().getProfileImg()).isNull();
        }

        @Test
        @DisplayName("프로필 이미지가 없으면 EntityNotFoundException 발생한다.")
        void failRemoveProfileImg() {
            initUser();

            Long invalidProfileImgId = 100L;

            // when
            assertThatThrownBy(
                    () -> profileImgService.removeProfileImg(invalidProfileImgId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

}