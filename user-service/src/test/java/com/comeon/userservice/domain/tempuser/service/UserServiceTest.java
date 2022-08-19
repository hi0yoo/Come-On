package com.comeon.userservice.domain.tempuser.service;

import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.Role;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.domain.user.service.UserService;
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
                    .name(name)
                    .email(email)
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
            user.authorize(Role.USER);

            userRepository.save(user);

            String newName = "newTestName";
            String newProfileImgUrl = "newProfileImgUrl";

            // when
             UserDto savedUserDto= userService.saveUser(
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
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            // when
            UserDto savedUserDto = userService.saveUser(
                    UserDto.builder()
                            .oauthId(oauthId)
                            .provider(provider)
                            .name(name)
                            .email(email)
                            .build()
            );
            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);

            // then
            assertThat(findUser.getOauthId()).isEqualTo(oauthId);
            assertThat(findUser.getProvider()).isEqualTo(provider);
            assertThat(findUser.getEmail()).isEqualTo(email);
            assertThat(findUser.getName()).isEqualTo(name);
            assertThat(findUser.getProfileImgUrl()).isNull();
        }
    }

}