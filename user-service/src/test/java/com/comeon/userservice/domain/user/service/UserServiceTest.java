package com.comeon.userservice.domain.user.service;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.dto.AccountDto;
import com.comeon.userservice.domain.user.dto.UserDto;
import com.comeon.userservice.domain.user.entity.Account;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.Status;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.domain.user.service.config.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

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
                    .accountDto(
                            AccountDto.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();

            // when
            UserDto savedUserDto = userService.saveUser(userDto);
            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);
            Account account = findUser.getAccount();

            // then
            assertThat(findUser).isNotNull();
            assertThat(account.getOauthId()).isEqualTo(oauthId);
            assertThat(account.getProvider()).isEqualTo(provider);
            assertThat(account.getEmail()).isEqualTo(email);
            assertThat(account.getName()).isEqualTo(name);
            assertThat(account.getProfileImgUrl()).isEqualTo(profileImgUrl);
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
                    .account(
                            Account.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();

            userRepository.save(user);

            String newName = "newTestName";
            String newProfileImgUrl = "newProfileImgUrl";

            // when
            UserDto savedUserDto = userService.saveUser(
                    UserDto.builder()
                            .accountDto(
                                    AccountDto.builder()
                                            .oauthId(oauthId)
                                            .provider(provider)
                                            .email(email)
                                            .name(newName)
                                            .profileImgUrl(newProfileImgUrl)
                                            .build()
                            )
                            .build()
            );

            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);
            Account account = findUser.getAccount();

            // then
            assertThat(findUser).isNotNull();
            assertThat(account.getOauthId()).isEqualTo(oauthId);
            assertThat(account.getProvider()).isEqualTo(provider);
            assertThat(account.getEmail()).isEqualTo(email);

            assertThat(account.getName()).isNotEqualTo(name);
            assertThat(account.getProfileImgUrl()).isNotEqualTo(profileImgUrl);

            assertThat(account.getName()).isEqualTo(newName);
            assertThat(account.getProfileImgUrl()).isEqualTo(newProfileImgUrl);
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
                            .accountDto(
                                    AccountDto.builder()
                                            .oauthId(oauthId)
                                            .provider(provider)
                                            .email(email)
                                            .name(name)
                                            .profileImgUrl(profileImgUrl)
                                            .build()
                            )
                            .build()
            );
            User findUser = userRepository.findById(savedUserDto.getId()).orElse(null);
            Account account = findUser.getAccount();

            // then
            assertThat(findUser).isNotNull();
            assertThat(account.getOauthId()).isEqualTo(oauthId);
            assertThat(account.getProvider()).isEqualTo(provider);
            assertThat(account.getEmail()).isEqualTo(email);
            assertThat(account.getName()).isEqualTo(name);
            assertThat(account.getProfileImgUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("유저 정보 조회")
    class findUser {

        @Test
        @DisplayName("success - 존재하는 유저의 식별자가 파라미터로 넘어오면 조회한 유저의 정보를 Dto로 반환한다.")
        void successFindUser() {
            // given
            String profileImgUrl = "profileImgUrl";
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            User user = User.builder()
                    .account(
                            Account.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();
            userRepository.save(user);

            UserDto userDto = userService.findUser(user.getId());
            AccountDto accountDto = userDto.getAccountDto();
            Account account = user.getAccount();

            assertThat(userDto.getId()).isEqualTo(user.getId());
            assertThat(accountDto.getOauthId()).isEqualTo(account.getOauthId());
            assertThat(accountDto.getProvider()).isEqualTo(account.getProvider());
            assertThat(accountDto.getEmail()).isEqualTo(account.getEmail());
            assertThat(accountDto.getName()).isEqualTo(account.getName());
            assertThat(accountDto.getProfileImgUrl()).isEqualTo(account.getProfileImgUrl());
            assertThat(userDto.getNickname()).isEqualTo(user.getNickname());
            assertThat(userDto.getProfileImgUrl()).isEqualTo(user.getProfileImgUrl());
            assertThat(userDto.getRole()).isEqualTo(user.getRole());
        }

        @Test
        @DisplayName("fail - 존재하지 않은 유저 식별자가 파라미터로 넘어오면, EntityNotFoundException이 발생한다.")
        void throwEntityNotFoundException() {
            assertThatThrownBy(
                    () -> userService.findUser(100L)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class withdrawUser {

        @Autowired
        AccountRepository accountRepository;

        @Autowired
        EntityManager em;

        @Test
        @DisplayName("success - 회원 탈퇴에 성공하면 유저의 Account 정보가 삭제되고, status가 ACTIVATE -> WITHDRAWN 변경된다.")
        void withdrawUserSuccess() {
            // given
            String profileImgUrl = "profileImgUrl";
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            User user = User.builder()
                    .account(
                            Account.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();
            userRepository.save(user);
            Long accountId = user.getAccount().getId();

            // 지금은 account 조회에 성공해야 한다.
            Optional<Account> optionalAccount = accountRepository.findById(accountId);
            assertThat(optionalAccount.isPresent()).isTrue();

            // when
            userService.withdrawUser(user.getId());
            em.flush();

            // then
            assertThat(user.getAccount()).isNull();
            assertThat(user.getStatus()).isNotEqualTo(Status.ACTIVATE);
            assertThat(user.getStatus()).isEqualTo(Status.WITHDRAWN);

            // account가 삭제되어야 한다.
            Optional<Account> afterWithdrawAccount = accountRepository.findById(accountId);
            assertThat(afterWithdrawAccount.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("fail - 파라미터로 넘어온 회원 식별자로 조회시, 해당 회원이 존재하지 않으면 EntityNotFoundException 발생")
        void withdrawUserFail() {
            Long invalidUserId = 100L;
            assertThatThrownBy(
                    () -> userService.withdrawUser(invalidUserId)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("탈퇴 처리된 회원은 더 이상 조회되지 않고 존재하지 않는 회원으로 간주된다.")
        void afterWithdrawal() {
            // given
            String profileImgUrl = "profileImgUrl";
            String oauthId = "oauthId";
            OAuthProvider provider = OAuthProvider.KAKAO;
            String name = "testName";
            String email = "testEmail";

            User user = User.builder()
                    .account(
                            Account.builder()
                                    .oauthId(oauthId)
                                    .provider(provider)
                                    .email(email)
                                    .name(name)
                                    .profileImgUrl(profileImgUrl)
                                    .build()
                    )
                    .build();
            userRepository.save(user);
            Long userId = user.getId();

            // when
            userService.withdrawUser(userId);
            em.flush();

            // then
            assertThatThrownBy(
                    () -> userService.findUser(userId)
            ).isInstanceOf(EntityNotFoundException.class);

            assertThat(userRepository.findById(userId).isEmpty()).isTrue();
        }
    }

}