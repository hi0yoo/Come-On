package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.docs.utils.RestDocsUtil;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.entity.UserRole;
import com.comeon.userservice.domain.user.entity.UserStatus;
import com.comeon.userservice.domain.user.service.UserService;
import com.comeon.userservice.domain.user.service.dto.UserAccountDto;
import com.comeon.userservice.web.AbstractControllerTest;
import com.comeon.userservice.web.common.aop.ValidationAspect;
import com.comeon.userservice.web.common.response.ListResponse;
import com.comeon.userservice.web.feign.authservice.AuthFeignService;
import com.comeon.userservice.web.user.query.UserQueryService;
import com.comeon.userservice.web.user.request.UserModifyRequest;
import com.comeon.userservice.web.user.request.UserSaveRequest;
import com.comeon.userservice.web.user.response.UserDetailResponse;
import com.comeon.userservice.web.user.response.UserSimpleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({
        AopAutoConfiguration.class,
        ValidationAspect.class
})
@WebMvcTest(UserController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class UserControllerTest extends AbstractControllerTest {

    private static final String TOKEN_TYPE_BEARER = "Bearer ";

    @MockBean
    UserService userService;

    @MockBean
    UserQueryService userQueryService;

    @MockBean
    AuthFeignService authFeignService;

    @Nested
    @DisplayName("유저 등록")
    class userSave {

        @Test
        @DisplayName("요청 데이터 검증에 성공하면 유저 정보를 저장하고, 저장된 유저 정보를 응답으로 반환한다. 등록된 프로필 이미지가 없으면 profileImg는 null 응답")
        void successWithNoProfileImg() throws Exception {
            // given
            String oauthId = "12345";
            String providerName = "kakao".toUpperCase();
            String name = "testName1";
            String email = "email1@email.com";
            UserSaveRequest userSaveRequest = new UserSaveRequest(
                    oauthId,
                    OAuthProvider.valueOf(providerName),
                    name,
                    email,
                    null
            );

            User user = setUser(oauthId, providerName, name, email, null);

            // mocking
            given(userService.saveUser(any(UserAccountDto.class)))
                    .willReturn(1L);
            given(userQueryService.getUserDetails(anyLong()))
                    .willReturn(new UserDetailResponse(user, null));

            // when
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(userSaveRequest))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(name))
                    .andExpect(jsonPath("$.data.role").value(UserRole.USER.getRoleValue()))
                    .andExpect(jsonPath("$.data.email").value(email))
                    .andExpect(jsonPath("$.data.name").value(name))
                    .andExpect(jsonPath("$.data.profileImg").isEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("oauthId").type(JsonFieldType.STRING).description("소셜 로그인 성공시, 서비스 제공 벤더로부터 응답받은 유저 ID 값"),
                                    fieldWithPath("provider").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.OAUTH_PROVIDER)),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일 정보"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름 또는 닉네임 정보"),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저 프로필 이미지 URL").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("저장된 유저의 닉네임"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("저장된 유저의 이름 정보"),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("저장된 유저의 소셜 이메일"),
                                    fieldWithPath("role").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_ROLE)),
                                    subsectionWithPath("profileImg").type(JsonFieldType.OBJECT).description("유저의 프로필 이미지").optional(),
                                    subsectionWithPath("profileImg.id").type(JsonFieldType.NUMBER).description("프로필 이미지의 식별값").optional(),
                                    subsectionWithPath("profileImg.imageUrl").type(JsonFieldType.STRING).description("프로필 이미지의 URL").optional()
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 성공하면 유저 정보를 저장하고, 저장된 유저 정보를 응답으로 반환한다. 프로필 이미지가 있으면 프로필 이미지 정보를 포함")
        void successWithProfileImg() throws Exception {
            // given
            String oauthId = "12345";
            String providerName = "kakao".toUpperCase();
            String name = "testName1";
            String email = "email1@email.com";
            UserSaveRequest userSaveRequest = new UserSaveRequest(
                    oauthId,
                    OAuthProvider.valueOf(providerName),
                    name,
                    email,
                    null
            );

            User user = setUser(oauthId, providerName, name, email, null);
            setProfileImg(user);

            // mocking
            given(userService.saveUser(any(UserAccountDto.class)))
                    .willReturn(1L);
            String fileUrl = fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName);
            given(userQueryService.getUserDetails(anyLong()))
                    .willReturn(new UserDetailResponse(user, fileUrl));

            // when
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(userSaveRequest))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.nickname").value(name))
                    .andExpect(jsonPath("$.data.role").value(UserRole.USER.getRoleValue()))
                    .andExpect(jsonPath("$.data.email").value(email))
                    .andExpect(jsonPath("$.data.name").value(name))
                    .andExpect(jsonPath("$.data.profileImg").isNotEmpty())
                    .andExpect(jsonPath("$.data.profileImg.id").isNotEmpty())
                    .andExpect(jsonPath("$.data.profileImg.imageUrl").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("oauthId").type(JsonFieldType.STRING).description("소셜 로그인 성공시, 서비스 제공 벤더로부터 응답받은 유저 ID 값"),
                                    fieldWithPath("provider").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.OAUTH_PROVIDER)),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일 정보"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름 또는 닉네임 정보"),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저 프로필 이미지 URL").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("저장된 유저의 닉네임"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("저장된 유저의 이름 정보"),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("저장된 유저의 소셜 이메일"),
                                    fieldWithPath("role").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_ROLE)),
                                    subsectionWithPath("profileImg").type(JsonFieldType.OBJECT).description("유저의 프로필 이미지").optional(),
                                    subsectionWithPath("profileImg.id").type(JsonFieldType.NUMBER).description("프로필 이미지의 식별값").optional(),
                                    subsectionWithPath("profileImg.imageUrl").type(JsonFieldType.STRING).description("프로필 이미지의 URL").optional()
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면 http status 400 반환한다.")
        void validationFail() throws Exception {
            // given
            UserSaveRequest request = new UserSaveRequest();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("유저 단건 조회")
    class userDetails {

        @Test
        @DisplayName("존재하는 유저의 식별값으로 조회하고 해당 유저가 ACTIVATE 상태라면, 유저의 id, nickname, profileImgUrl, status 정보를 응답한다.")
        void activateUser() throws Exception {
            // given
            User user = setUser();
            setProfileImg(user);

            Long userId = user.getId();

            String fileUrl = fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName);

            // mocking
            given(userQueryService.getUserSimple(userId))
                    .willReturn(new UserSimpleResponse(user, fileUrl));

            // when
            String path = "/users/{userId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                    .andExpect(jsonPath("$.data.profileImgUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.status").value(UserStatus.ACTIVATE.name()));

            // docs
            perform.andDo(
                    restDocs.document(
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("userId").description("조회할 유저의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임").optional(),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지").optional(),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_STATUS))
                            )
                    )
            );
        }

        @Test
        @DisplayName("ACTIVATE 유저는 프로필 이미지가 없을 수 있다.")
        void activateUserWithNoProfileImg() throws Exception {
            // given
            User user = setUser();

            Long userId = user.getId();

            // mocking
            given(userQueryService.getUserSimple(userId))
                    .willReturn(new UserSimpleResponse(user, null));

            // when
            String path = "/users/{userId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                    .andExpect(jsonPath("$.data.profileImgUrl").isEmpty())
                    .andExpect(jsonPath("$.data.status").value(UserStatus.ACTIVATE.name()));

            // docs
            perform.andDo(
                    restDocs.document(
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("userId").description("조회할 유저의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임").optional(),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지").optional(),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_STATUS))
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하는 유저의 식별값으로 조회하고 해당 유저가 WITHDRAW 상태라면, 유저의 id, status 정보만 응답한다. nickname, profileImgUrl 필드는 null")
        void withDrawnUser() throws Exception {
            // given
            User user = setUser();
            user.withdrawal();

            Long userId = user.getId();

            // mocking
            given(userQueryService.getUserSimple(userId))
                    .willReturn(new UserSimpleResponse(user));

            // when
            String path = "/users/{userId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").isEmpty())
                    .andExpect(jsonPath("$.data.profileImgUrl").isEmpty())
                    .andExpect(jsonPath("$.data.status").value(UserStatus.WITHDRAWN.name()));

            // docs
            perform.andDo(
                    restDocs.document(
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("userId").description("조회할 유저의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저 닉네임").optional(),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("등록된 유저 프로필 이미지").optional(),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_STATUS))
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 유저의 식별값으로 조회하면, http status 400 반환한다.")
        void invalidUser() throws Exception {
            // give
            Long invaildUserId = 100L;

            // mocking
            given(userQueryService.getUserSimple(invaildUserId))
                    .willThrow(new EntityNotFoundException("해당 식별자를 가진 User가 없습니다. 요청한 User 식별값 : " + invaildUserId));

            // when
            String path = "/users/{userId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, invaildUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.ENTITY_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.ENTITY_NOT_FOUND.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("유저 리스트 조회")
    class userList {

        @Test
        @DisplayName("유저 리스트 조회에 성공한다.")
        void success() throws Exception {
            // given
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                User user = setUser();
                setProfileImg(user);

                if (i % 5 == 0) {
                    user.withdrawal();
                }
                userList.add(user);
            }

            List<Long> userIds = userList.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            given(userQueryService.getUserList(userIds))
                    .will(invocation -> ListResponse.toListResponse(
                                    userList.stream()
                                            .map(user -> {
                                                        if (user.isActivateUser()) {
                                                            return UserSimpleResponse.activateUserResponseBuilder()
                                                                    .user(user)
                                                                    .profileImgUrl(fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName))
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
                    restDocs.document(
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("userIds").description("조회할 유저 식별값 리스트")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("count").type(JsonFieldType.NUMBER).description("조회한 유저의 수"),
                                    subsectionWithPath("contents").type(JsonFieldType.ARRAY).description("조회한 유저 정보 리스트")
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("contents 응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저의 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저의 닉네임").optional(),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저의 프로필 이미지 URL").optional(),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_STATUS))
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 유저의 식별값은 결과에서 제외된다.")
        void ignoreNotExistUserIds() throws Exception {
            // given
            List<User> userList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                User user = setUser();
                setProfileImg(user);

                if (i % 5 == 0) {
                    user.withdrawal();
                }
                userList.add(user);
            }

            List<Long> userIds = userList.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

            given(userQueryService.getUserList(userIds))
                    .will(invocation -> ListResponse.toListResponse(
                                    userList.stream()
                                            .map(user -> {
                                                        if (user.isActivateUser()) {
                                                            return UserSimpleResponse.activateUserResponseBuilder()
                                                                    .user(user)
                                                                    .profileImgUrl(fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName))
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
            List<Long> invalidUserIds = List.of(100L, 200L);
            userIds.addAll(invalidUserIds);
            String params = userIds.stream().map(Object::toString).collect(Collectors.joining(","));
            ResultActions perform = mockMvc.perform(
                    MockMvcRequestBuilders.get("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .queryParam("userIds", params)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(userIds.size() - invalidUserIds.size()))
                    .andExpect(jsonPath("$.data.contents").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("userIds").description("조회할 유저 식별값 리스트")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("count").type(JsonFieldType.NUMBER).description("조회한 유저의 수"),
                                    subsectionWithPath("contents").type(JsonFieldType.ARRAY).description("조회한 유저 정보 리스트")
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("contents 응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("유저의 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("유저의 닉네임").optional(),
                                    fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저의 프로필 이미지 URL").optional(),
                                    fieldWithPath("status").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_STATUS))
                            )
                    )
            );
        }

        @Test
        @DisplayName("파라미터로 아무것도 넘어오지 않으면 요청에 실패하고 http status 400 반환한다.")
        void noParamsError() throws Exception {
            // given

            // when
            ResultActions perform = mockMvc.perform(
                    MockMvcRequestBuilders.get("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").isNotEmpty());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class myDetails {

        @Test
        @DisplayName("현재 유저의 토큰을 통해 유저의 상세 정보 조회에 성공한다.")
        void success() throws Exception {
            // given
            User user = setUser();
            setProfileImg(user);

            Long userId = user.getId();

            String fileUrl = fileManager.getFileUrl(user.getProfileImg().getStoredName(), dirName);

            String accessToken = generateUserAccessToken(userId);

            // mocking
            given(userQueryService.getUserDetails(userId))
                    .willReturn(new UserDetailResponse(user, fileUrl));

            // when
            ResultActions perform = mockMvc.perform(
                    get("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                    .andExpect(jsonPath("$.data.role").value(user.getRole().getRoleValue()))
                    .andExpect(jsonPath("$.data.email").value(user.getAccount().getEmail()))
                    .andExpect(jsonPath("$.data.name").value(user.getAccount().getName()))
                    .andExpect(jsonPath("$.data.profileImg").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("저장된 유저의 닉네임"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("저장된 유저의 이름 정보"),
                                    fieldWithPath("email").type(JsonFieldType.STRING).description("저장된 유저의 소셜 이메일"),
                                    fieldWithPath("role").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.USER_ROLE)),
                                    subsectionWithPath("profileImg").type(JsonFieldType.OBJECT).description("유저의 프로필 이미지").optional(),
                                    subsectionWithPath("profileImg.id").type(JsonFieldType.NUMBER).description("프로필 이미지의 식별값").optional(),
                                    subsectionWithPath("profileImg.imageUrl").type(JsonFieldType.STRING).description("프로필 이미지의 URL").optional()
                            )
                    )
            );
        }
    }


    @Nested
    @DisplayName("유저 정보 수정")
    class userModify {

        @Test
        @DisplayName("유저 정보 변경에 성공하면 수정 성공 메시지를 응답한다.")
        void success() throws Exception {
            // given
            User user = setUser();

            Long userId = user.getId();

            String newNickname = "새로운닉네임";
            UserModifyRequest request = new UserModifyRequest(newNickname);

            String accessToken = generateUserAccessToken(userId);

            // mocking
            willDoNothing().given(userService).modifyUser(eq(userId), any());

            // when
            ResultActions perform = mockMvc.perform(
                    patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("변경할 닉네임")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("요청 처리 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면 http status 400 반환한다.")
        void validationFail() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            UserModifyRequest request = new UserModifyRequest();

            // when
            ResultActions perform = mockMvc.perform(
                    patch("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE_BEARER + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class userWithdraw {

        @Test
        @DisplayName("회원 탈퇴 요청을 성공적으로 처리하면, 요청 성공 처리 메시지를 응답한다.")
        void success() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            // mocking
            given(userQueryService.getUserOauthId(userId))
                    .willReturn(Long.parseLong(user.getAccount().getOauthId()));
            willDoNothing().given(userService).withdrawUser(userId);
            willDoNothing().given(authFeignService)
                    .userUnlink(anyString(), anyLong());

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
            );

            // then
            perform.andExpect(status().isOk());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("요청 성공 메세지")
                            )
                    )
            );
        }
        
        @Test
        @DisplayName("auth-service 내부 로직 오류 발생 또는 이용 불가상태이면 http status 500 반환")
        void authServiceError() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);
            
            // mocking
            given(userQueryService.getUserOauthId(userId))
                    .willReturn(Long.parseLong(user.getAccount().getOauthId()));
            willThrow(new CustomException("error", ErrorCode.AUTH_SERVICE_ERROR))
                    .given(authFeignService)
                    .userUnlink(anyString(), anyLong());

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
            );

            // then
            perform.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.AUTH_SERVICE_ERROR.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.AUTH_SERVICE_ERROR.getMessage()));

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("카카오 API 오류 응답을 받으면 http status 500 반환한다.")
        void kakaoApiError() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            // mocking
            given(userQueryService.getUserOauthId(userId))
                    .willReturn(Long.parseLong(user.getAccount().getOauthId()));
            willThrow(new CustomException("error", ErrorCode.KAKAO_API_ERROR))
                    .given(authFeignService)
                    .userUnlink(anyString(), anyLong());

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
            );

            // then
            perform.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.KAKAO_API_ERROR.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.KAKAO_API_ERROR.getMessage()));

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("oauthId 관련 오류 응답은 ErrorCode.SERVER_ERROR")
        void userServiceError() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            // mocking
            given(userQueryService.getUserOauthId(userId))
                    .willReturn(Long.parseLong(user.getAccount().getOauthId()));
            willThrow(new CustomException("error", ErrorCode.SERVER_ERROR))
                    .given(authFeignService)
                    .userUnlink(anyString(), anyLong());

            // when
            ResultActions perform = mockMvc.perform(
                    delete("/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
            );

            // then
            perform.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.SERVER_ERROR.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.SERVER_ERROR.getMessage()));

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }
}
