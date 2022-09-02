package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserAccount;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.web.common.response.ListResponse;
import com.comeon.userservice.web.feign.authservice.AuthServiceFeignClient;
import com.comeon.userservice.web.user.query.UserQueryService;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureRestDocs
@WebMvcTest(UserController.class)
@ExtendWith(RestDocumentationExtension.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerTestV2 {

    @MockBean
    AuthServiceFeignClient authServiceFeignClient;

    @MockBean
    UserService userService;

    @MockBean
    UserQueryService userQueryService;

    @Autowired
    UserController userController;

    @Autowired
    MockMvc mockMvc;

    @Nested
    @DisplayName("회원 리스트 조회")
    class userList {

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

        // TODO 탈퇴한 회원 처리
        @Test
        @DisplayName("[docs] 파라미터로 넘어온 데이터가 있으면, 유저 리스트 조회에 성공한다.")
        void success() throws Exception {
            // given
            initUserList();

            users.stream()
                    .filter(user -> user.getId() % 2 == 0)
                    .forEach(User::withdrawal);

            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            given(userQueryService.getUserList(userIds))
                    .will(invocation -> ListResponse.toListResponse(
                                    users.stream()
                                            .map(user -> {
                                                        if (user.isActivateUser()) {
                                                            return UserSimpleResponse.activateUserResponseBuilder()
                                                                    .user(user)
                                                                    .profileImgUrl("http://file-domain/" + user.getProfileImg().getStoredName())
                                                                    .build();
                                                        }
                                                        return UserSimpleResponse.withdrawnUserResponseBuilder()
                                                                .user(user)
                                                                .build();
                                                    }
                                            ).collect(Collectors.toList())
                            )
                    );

            // when
            String params = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
            ResultActions perform = mockMvc.perform(
                    MockMvcRequestBuilders.get("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .queryParam("userIds", params)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(userIds.size()))
                    .andExpect(jsonPath("$.data.contents").isNotEmpty());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("userIds").description("조회할 유저 식별값 리스트")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("count").type(JsonFieldType.NUMBER).description("조회한 유저의 수"),
                                    fieldWithPath("contents").type(JsonFieldType.ARRAY).description("조회한 유저 정보 리스트"),
                                    fieldWithPath("contents[].userId").type(JsonFieldType.NUMBER).description("유저의 식별값"),
                                    fieldWithPath("contents[].nickname").type(JsonFieldType.STRING).description("유저의 닉네임").optional(),
                                    fieldWithPath("contents[].profileImgUrl").type(JsonFieldType.STRING).description("유저의 프로필 이미지 URL").optional(),
                                    fieldWithPath("contents[].status").type(JsonFieldType.STRING).description("유저의 상태")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 파라미터로 넘어온 데이터가 없으면, http status 400 반환한다.")
        void fail() throws Exception {
            // given
            initUserList();
            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            given(userQueryService.getUserList(userIds))
                    .will(invocation -> ListResponse.toListResponse(
                                    users.stream()
                                            .map(user -> {
                                                        if (user.isActivateUser()) {
                                                            return UserSimpleResponse.activateUserResponseBuilder()
                                                                    .user(user)
                                                                    .profileImgUrl("http://file-domain/" + user.getProfileImg().getStoredName())
                                                                    .build();
                                                        }
                                                        return UserSimpleResponse.withdrawnUserResponseBuilder()
                                                                .user(user)
                                                                .build();
                                                    }
                                            ).collect(Collectors.toList())
                            )
                    );

            // when
            String params = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
            ResultActions perform = mockMvc.perform(
                    MockMvcRequestBuilders.get("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message.userIds").exists());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("애플리케이션 내부 에러 코드"),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("애플리케이션 내부 에러 메시지"),
                                    fieldWithPath("message.userIds").type(JsonFieldType.ARRAY).description("에러 발생 파라미터 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 유저 식별값은 결과에서 제외된다.")
        void ignoreNotExistUserId() throws Exception {
            // given
            initUserList();
            List<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            userIds.add(100L);
            userIds.add(101L); // userIds = 1, 2, 3, 4, 5, 100, 101
            // user = 2, 3, 4, 5
            userIds.remove(users.stream().mapToLong(User::getId).filter(value -> value == 1).findFirst().orElseThrow());

            given(userQueryService.getUserList(userIds))
                    .will(invocation -> ListResponse.toListResponse(
                                    users.stream()
                                            .filter(user -> userIds.contains(user.getId()))
                                            .map(user -> {
                                                        if (user.isActivateUser()) {
                                                            return UserSimpleResponse.activateUserResponseBuilder()
                                                                    .user(user)
                                                                    .profileImgUrl("http://file-domain/" + user.getProfileImg().getStoredName())
                                                                    .build();
                                                        }
                                                        return UserSimpleResponse.withdrawnUserResponseBuilder()
                                                                .user(user)
                                                                .build();
                                                    }
                                            ).collect(Collectors.toList())
                            )
                    );

            // when
            String params = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
            ResultActions perform = mockMvc.perform(
                    MockMvcRequestBuilders.get("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .queryParam("userIds", params)
            );

            List<Long> savedUserIdList = users.stream()
                    .map(User::getId)
                    .filter(userIds::contains)
                    .collect(Collectors.toList());
            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(savedUserIdList.size()))
                    .andExpect(jsonPath("$.data.contents").isNotEmpty());
        }
    }
}
