package com.comeon.userservice.domain.user.service;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("유저 정보 저장")
    class saveUser {

        @Test
        @DisplayName("success - 올바른 정보를 가진 신규 유저의 경우, 데이터를 새로 등록한다.")
        void saveUser_success() {
            // given
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";
            String profileImgUrl = "profileImgUrl";

            UserDto userDto = UserDto.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .email(email)
                    .name(name)
                    .profileImgUrl(profileImgUrl)
                    .build();

            // when
            UserDto savedUserDto = userService.saveUser(userDto);
            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);

            // then
            assertThat(findUser).isNotNull();
            assertThat(findUser.getOauthId()).isEqualTo(oauthId);
            assertThat(findUser.getProvider()).isEqualTo(provider);
            assertThat(findUser.getEmail()).isEqualTo(email);
            assertThat(findUser.getName()).isEqualTo(name);
            assertThat(findUser.getProfileImgUrl()).isEqualTo(profileImgUrl);
        }

        @Test
        @DisplayName("success - 올바른 정보를 가진 기존 유저의 경우, 변경된 데이터를 수정한다.")
        void saveUser_success_update() {
            // given
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";
            String profileImgUrl = "profileImgUrl";

            User user = User.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .email(email)
                    .name(name)
                    .profileImgUrl(profileImgUrl)
                    .build();

            userRepository.save(user);

            String newName = "newTestName";
            String newProfileImgUrl = "newProfileImgUrl";

            // when
            UserDto savedUserDto = userService.saveUser(
                    UserDto.builder()
                            .oauthId(oauthId)
                            .provider(provider)
                            .email(email)
                            .name(newName)
                            .profileImgUrl(newProfileImgUrl)
                            .build()
            );

            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);

            // then
            assertThat(findUser).isNotNull();
            assertThat(findUser.getOauthId()).isEqualTo(oauthId);
            assertThat(findUser.getProvider()).isEqualTo(provider);
            assertThat(findUser.getEmail()).isEqualTo(email);

            assertThat(findUser.getName()).isNotEqualTo(name);
            assertThat(findUser.getProfileImgUrl()).isNotEqualTo(profileImgUrl);

            assertThat(findUser.getName()).isEqualTo(newName);
            assertThat(findUser.getProfileImgUrl()).isEqualTo(newProfileImgUrl);
        }

        @Test
        @DisplayName("success - profileImgUrl 필드는 null 이어도 성공한다.")
        void saveUser_success_noProfileImgUrl() {
            // given
            String profileImgUrl = null;
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            // when
            UserDto savedUserDto = userService.saveUser(
                    UserDto.builder()
                            .oauthId(oauthId)
                            .provider(provider)
                            .email(email)
                            .name(name)
                            .profileImgUrl(profileImgUrl)
                            .build()
            );
            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);

            // then
            assertThat(findUser).isNotNull();
            assertThat(findUser.getOauthId()).isEqualTo(oauthId);
            assertThat(findUser.getProvider()).isEqualTo(provider);
            assertThat(findUser.getEmail()).isEqualTo(email);
            assertThat(findUser.getName()).isEqualTo(name);
            assertThat(findUser.getProfileImgUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("유저 정보 조회")
    class findUser {

        @Test
        @DisplayName("존재하는 유저의 식별자가 파라미터로 넘어오면 조회한 유저의 정보를 Dto로 반환한다.")
        void successFindUser() {
            // given
            String profileImgUrl = "profileImgUrl";
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            User user = User.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .email(email)
                    .name(name)
                    .profileImgUrl(profileImgUrl)
                    .build();
            userRepository.save(user);

            UserDto userDto = userService.findUser(user.getId());

            assertThat(userDto.getId()).isEqualTo(user.getId());
            assertThat(userDto.getOauthId()).isEqualTo(user.getOauthId());
            assertThat(userDto.getProvider()).isEqualTo(user.getProvider());
            assertThat(userDto.getEmail()).isEqualTo(user.getEmail());
            assertThat(userDto.getName()).isEqualTo(user.getName());
            assertThat(userDto.getProfileImgUrl()).isEqualTo(user.getProfileImgUrl());
            assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
            assertThat(userDto.getRole()).isEqualTo(user.getRole());
        }

        @Test
        @DisplayName("존재하지 않은 유저 식별자가 파라미터로 넘어오면, EntityNotFoundException이 발생한다.")
        void throwEntityNotFoundException() {
            assertThatThrownBy(
                    () -> userService.findUser(100L)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

}