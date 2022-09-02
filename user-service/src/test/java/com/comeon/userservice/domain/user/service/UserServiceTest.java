package com.comeon.userservice.domain.user.service;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.entity.*;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.domain.user.service.config.AccountRepository;
import com.comeon.userservice.domain.user.service.dto.ModifyUserInfoFields;
import com.comeon.userservice.domain.user.service.dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;


@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EntityManager em;

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

    @Nested
    @DisplayName("유저 정보 저장")
    class saveUser {

        @Test
        @DisplayName("주어진 oauthId와 provider가 일치하는 유저가 없으면, 유저를 새로 생성한다. 새로 저장된 유저의 id를 반환한다.")
        void createUser() {
            // given
            UserAccountDto accountDto = UserAccountDto.builder()
                    .oauthId("oauthId")
                    .provider(OAuthProvider.KAKAO)
                    .email("email")
                    .name("name")
                    .build();

            // when
            Long savedUserId = userService.saveUser(accountDto);
            em.flush();

            // then
            User savedUser = userRepository.findById(savedUserId).orElseThrow();
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId()).isEqualTo(savedUserId);

            UserAccount account = savedUser.getAccount();
            assertThat(account.getOauthId()).isEqualTo(accountDto.getOauthId());
            assertThat(account.getProvider()).isEqualTo(accountDto.getProvider());
            assertThat(account.getEmail()).isEqualTo(accountDto.getEmail());
            assertThat(account.getName()).isEqualTo(accountDto.getName());

            assertThat(savedUser.getNickname()).isNotNull();
            // 처음 등록하면 이름으로 닉네임이 등록된다.
            assertThat(savedUser.getNickname()).isEqualTo(accountDto.getName());
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVATE);
        }

        @Test
        @DisplayName("주어진 oauthId와 provider가 일치하는 유저가 있으면, 변경된 정보를 수정한다. 해당 유저의 id를 반환한다.")
        void updateUser() {
            // given
            initUser();
            Long originalUserId = user.getId();

            UserAccountDto accountDto = UserAccountDto.builder()
                    .oauthId("oauthId")
                    .provider(OAuthProvider.KAKAO)
                    .email("newEmail")
                    .name("newName")
                    .build();

            // when
            Long savedUserId = userService.saveUser(accountDto);
            em.flush();

            // then
            User savedUser = userRepository.findById(savedUserId).orElseThrow();

            assertThat(originalUserId).isEqualTo(savedUserId);
            assertThat(savedUser).isNotNull();
            // 변경된 정보가 저장된다.
            UserAccount account = savedUser.getAccount();
            assertThat(account.getEmail()).isEqualTo(accountDto.getEmail());
            assertThat(account.getName()).isEqualTo(accountDto.getName());
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class withdrawUser {

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하는 유저이고, 탈퇴되지 않았다면, Account 정보를 지우고 탈퇴 처리한다.")
        void withdrawSuccess() {
            // given
            initUser();
            Long userId = user.getId();
            Long accountId = user.getAccount().getId();
            em.flush();

            // when
            userService.withdrawUser(userId);
            em.flush();

            // then
            User withdrawnUser = userRepository.findById(userId).orElseThrow();
            assertThat(withdrawnUser).isNotNull();
            assertThat(withdrawnUser.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
            assertThat(withdrawnUser.getAccount()).isNull();
            // 기존 UserAccount 식별자로 조회되지 않아야 한다.
            assertThat(accountRepository.findById(accountId)).isNotPresent();
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하지만, 탈퇴된 회원이라면, CustomException 발생한다. 예외 내부에 ErrorCode.ALREADY_WITHDRAW 를 가진다.")
        void alreadyWithdrawn() {
            // given
            initUser();
            Long userId = user.getId();
            user.withdrawal();
            em.flush();

            // when, then
            assertThatThrownBy(
                    () -> userService.withdrawUser(userId)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하지 않는 유저의 식별값이면, EntityNotFoundException 발생한다.")
        void invalidUserId() {
            // given
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> userService.withdrawUser(invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("유저 정보 수정")
    class modifyUser {

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하는 유저이고, 탈퇴되지 않았다면, 지정한 필드를 수정한다.")
        void modifySuccess() {
            // given
            initUser();
            Long userId = user.getId();
            String originalNickname = user.getNickname();

            String newNickname = "newNickname";
            ModifyUserInfoFields modifyUserInfoFields = new ModifyUserInfoFields(newNickname);

            // when
            userService.modifyUser(userId, modifyUserInfoFields);
            em.flush();

            // then
            User modifiedUser = userRepository.findById(userId).orElseThrow();
            assertThat(modifiedUser).isNotNull();
            assertThat(modifiedUser.getNickname()).isNotEqualTo(originalNickname);
            assertThat(modifiedUser.getNickname()).isEqualTo(newNickname);
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하지만, 탈퇴된 회원이라면, CustomException 발생한다. 예외 내부에 ErrorCode.ALREADY_WITHDRAW 를 가진다.")
        void alreadyWithdrawn() {
            // given
            initUser();
            Long userId = user.getId();
            user.withdrawal();
            em.flush();

            // when, then
            assertThatThrownBy(
                    () -> userService.withdrawUser(userId)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAW);
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 존재하지 않는 유저의 식별값이면, EntityNotFoundException 발생한다.")
        void invalidUserId() {
            // given
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> userService.withdrawUser(invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

}