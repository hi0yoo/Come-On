package com.comeon.userservice.web.profileimage.controller;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.docs.utils.RestDocsUtil;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.domain.profileimage.service.ProfileImgService;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.web.AbstractControllerTest;
import com.comeon.userservice.web.common.aop.ValidationAspect;
import com.comeon.userservice.web.profileimage.controller.ProfileImgController;
import com.comeon.userservice.web.profileimage.query.ProfileImgQueryService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({
        AopAutoConfiguration.class,
        ValidationAspect.class
})
@WebMvcTest(ProfileImgController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class ProfileImgControllerTest extends AbstractControllerTest {

    @MockBean
    ProfileImgService profileImgService;

    @MockBean
    ProfileImgQueryService profileImgQueryService;

    @Nested
    @DisplayName("프로필 이미지 등록/수정")
    class profileImageSave {

        @Test
        @DisplayName("요청 데이터 검증에 성공하면 해당 이미지를 저장하고, 저장된 이미지의 url을 응답한다.")
        void success() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            MockMultipartFile mockMultipartFile = getMockMultipartFile("test-img.png");

            // mocking
            given(profileImgQueryService.getStoredFileNameByUserId(userId))
                    .willReturn(null);
            long expectProfileImgId = 1L;
            given(profileImgService.saveProfileImg(any(), eq(userId)))
                    .willReturn(expectProfileImgId);

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/profile-image")
                            .file(mockMultipartFile)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.profileImgId").value(expectProfileImgId))
                    .andExpect(jsonPath("$.data.imageUrl").isNotEmpty());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestParts(
                                    attributes(key("title").value("요청 파트")),
                                    partWithName("imgFile").description("등록할 프로필 이미지 파일")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("profileImgId").type(JsonFieldType.NUMBER).description("저장된 프로필 이미지 식별값"),
                                    subsectionWithPath("imageUrl").type(JsonFieldType.STRING).description("저장된 프로필 이미지 URL")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면 http status 400을 반환한다.")
        void validationFail() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/profile-image")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
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
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("요청 처리 성공 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제")
    class profileImageRemove {

        @Test
        @DisplayName("지정한 프로필 이미지 식별값을 갖는 프로필 이미지가 존재하고, 요청한 유저가 작성자이면, 이를 삭제하고 성공 메시지를 응답한다.")
        void success() throws Exception {
            // given
            User user = setUser();
            setProfileImg(user);

            Long profileImgId = user.getProfileImg().getId();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            // mocking
            given(profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(anyLong(), anyLong()))
                    .willReturn(user.getProfileImg().getStoredName());
            willDoNothing().given(profileImgService).removeProfileImg(anyLong());

            // when
            String path = "/profile-image/{profileImgId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, profileImgId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
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
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("profileImgId").description("프로필 이미지 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("요청 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않은 프로필 이미지 식별값을 지정하면 http status 400 반환한다.")
        void invalidProfileImgError() throws Exception {
            // given
            User user = setUser();
            Long userId = user.getId();
            String accessToken = generateUserAccessToken(userId);

            Long profileImgId = 100L;

            // mocking
            given(profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(eq(profileImgId), anyLong()))
                    .willThrow(new EntityNotFoundException("해당 식별자를 가진 ProfileImg가 없습니다. 요청한 ProfileImg 식별값 : " + profileImgId));

            // when
            String path = "/profile-image/{profileImgId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, profileImgId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.ENTITY_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.ENTITY_NOT_FOUND.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("profileImgId").description("프로필 이미지 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("지정한 프로필 이미지의 등록자가 아닌 유저가 요청하면, http status 403 반환한다.")
        void notWriterError() throws Exception {
            // given
            User user = setUser();
            setProfileImg(user);
            Long userId = 100L;
            String accessToken = generateUserAccessToken(userId);

            Long profileImgId = user.getProfileImg().getId();

            // mocking
            given(profileImgQueryService.getStoredFileNameByProfileImgIdAndUserId(anyLong(), eq(userId)))
                    .willThrow(new CustomException("요청을 수행 할 권한이 없습니다. 요청한 User 식별값 : " + userId, ErrorCode.NO_AUTHORITIES));

            // when
            String path = "/profile-image/{profileImgId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, profileImgId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.NO_AUTHORITIES.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORITIES.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("profileImgId").description("프로필 이미지 식별값")
                            ),
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
}
