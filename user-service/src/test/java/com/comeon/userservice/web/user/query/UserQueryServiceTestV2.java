package com.comeon.userservice.web.user.query;

import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.entity.UserStatus;
import com.comeon.userservice.web.common.file.FileManager;
import com.comeon.userservice.web.common.response.ListResponse;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UserQueryServiceTestV2 {

    @Mock
    FileManager fileManager;

    @Mock
    UserQueryRepository userQueryRepository;

    @InjectMocks
    UserQueryService userQueryService;

    @Nested
    @DisplayName("유저 리스트 조회")
    class getUserList {

        List<User> users = new ArrayList<>();

        private void initUserList() {
            int count = 5;
            for (int i = 1; i <= count; i++) {
                User user = User.builder()
                        .account(
                                UserAccount.builder()
                                        .oauthId("oauthId" + i)
                                        .provider(OAuthProvider.KAKAO)
                                        .email("email" + i + "@email.com")
                                        .name("userName" + i)
                                        .build()
                        )
                        .build();
                ReflectionTestUtils.setField(user, "id", (long) i);

                ProfileImg profileImg = ProfileImg.builder()
                        .user(user)
                        .originalName("originalFileName" + i)
                        .storedName("storedFileName" + i)
                        .build();
                user.updateProfileImg(
                        profileImg
                );
                ReflectionTestUtils.setField(profileImg, "id", (long) i);

                users.add(user);
            }
        }

        @Test
        @DisplayName("유저 리스트 조회 성공")
        void success() {
            //given
            initUserList();
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

            given(userQueryRepository.findByIdInIdListFetchProfileImg(userIds))
                    .willReturn(
                            users.stream()
                                    .filter(user -> userIds.contains(user.getId()))
                                    .collect(Collectors.toList())
                    );
            given(fileManager.getFileUrl(anyString(), any()))
                    .willReturn("profileImgUrl");

            //when
            ListResponse<UserSimpleResponse> listResponse = userQueryService.getUserList(userIds);

            //then
            List<UserSimpleResponse> contents = listResponse.getContents();
            for (UserSimpleResponse content : contents) {
                log.info("userId : {}", content.getUserId());
                log.info("nickname : {}", content.getNickname());
                log.info("status : {}", content.getStatus());
                log.info("profileImgUrl : {}", content.getProfileImgUrl());
            }
            assertThat(listResponse).isNotNull();
            assertThat(listResponse.getCount()).isEqualTo(users.size());
        }


        @Test
        @DisplayName("탈퇴 처리된 유저는 닉네임, 프로필 이미지 등의 정보가 없다.")
        void successFilteringWithdrawnUser() {
            //given
            initUserList();
            users.stream()
                    .filter(user -> user.getId() % 2 == 0)
                    .forEach(User::withdrawal);

            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

            given(userQueryRepository.findByIdInIdListFetchProfileImg(userIds))
                    .willReturn(
                            users.stream()
                                    .filter(user -> userIds.contains(user.getId()))
                                    .collect(Collectors.toList())
                    );
            given(fileManager.getFileUrl(anyString(), any()))
                    .willReturn("profileImgUrl");

            //when
            ListResponse<UserSimpleResponse> listResponse = userQueryService.getUserList(userIds);

            //then
            List<UserSimpleResponse> contents = listResponse.getContents();
            for (UserSimpleResponse content : contents) {
                log.info("userId : {}", content.getUserId());
                log.info("nickname : {}", content.getNickname());
                log.info("status : {}", content.getStatus());
                log.info("profileImgUrl : {}", content.getProfileImgUrl());
            }
            assertThat(listResponse).isNotNull();
            assertThat(listResponse.getCount()).isEqualTo(users.size());
            contents.stream()
                    .filter(userSimpleResponse -> userSimpleResponse.getUserId() % 2 == 0)
                    .forEach(userSimpleResponse -> {
                        assertThat(userSimpleResponse.getNickname()).isNull();
                        assertThat(userSimpleResponse.getProfileImgUrl()).isNull();
                        assertThat(userSimpleResponse.getStatus()).isEqualTo(UserStatus.WITHDRAWN.name());
                    });
            contents.stream()
                    .filter(userSimpleResponse -> userSimpleResponse.getUserId() % 2 != 0)
                    .forEach(userSimpleResponse -> {
                        assertThat(userSimpleResponse.getNickname()).isNotNull();
                        assertThat(userSimpleResponse.getProfileImgUrl()).isNotNull();
                        assertThat(userSimpleResponse.getStatus()).isEqualTo(UserStatus.ACTIVATE.name());
                    });
        }
    }
}
