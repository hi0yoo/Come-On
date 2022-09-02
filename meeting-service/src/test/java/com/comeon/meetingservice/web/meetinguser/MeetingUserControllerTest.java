package com.comeon.meetingservice.web.meetinguser;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.service.MeetingUserService;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserAddRequest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MeetingUserController.class)
class MeetingUserControllerTest extends ControllerTestBase {

    @MockBean
    MeetingUserService meetingUserService;

    @Nested
    @DisplayName("모임유저 저장")
    class 모임유저저장 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("유효한 코드와 유효한 회원을 등록할 경우 Created를 응답한다.")
            public void 정상흐름() throws Exception {

                String unexpiredInviteCode = "AAAAAA";
                Long unJoinedUserId = 10L;
                String unJoinedUserToken = createToken(unJoinedUserId);

                // 서비스 정상 시나리오 모킹
                MeetingUserAddDto meetingUserAddDto = MeetingUserAddDto.builder()
                        .inviteCode(unexpiredInviteCode)
                        .userId(unJoinedUserId)
                        .build();

                Long createdMeetingUserId = 20L;
                given(meetingUserService.add(refEq(meetingUserAddDto))).willReturn(createdMeetingUserId);

                // 요청 데이터
                MeetingUserAddRequest meetingUserAddRequest =
                        MeetingUserAddRequest.builder()
                                .inviteCode(unexpiredInviteCode)
                                .build();

                mockMvc.perform(post("/meetings/users")
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserAddRequest))
                        )

                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data", equalTo(createdMeetingUserId), Long.class))

                        .andDo(document("user-create-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("inviteCode").description("모임의 초대 코드").attributes(key("format").value("[영문 대문자], [숫자], [영문 대문자 + 숫자 조합] 문자열 6자리"))
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("유효기간이 지난 코드인 경우 BadRequest를 응답한다.")
            public void 예외_유효기간() throws Exception {

                String expiredInviteCode = "BBBBBB";
                Long unJoinedUserId = 10L;
                String unJoinedUserToken = createToken(unJoinedUserId);

                // 서비스 유효기간 예외 시나리오 모킹
                MeetingUserAddDto meetingUserAddDto = MeetingUserAddDto.builder()
                        .inviteCode(expiredInviteCode)
                        .userId(unJoinedUserId)
                        .build();

                willThrow(new CustomException("유효기간 만료", ErrorCode.EXPIRED_CODE))
                        .given(meetingUserService).add(refEq(meetingUserAddDto));

                MeetingUserAddRequest meetingUserAddRequest =
                        MeetingUserAddRequest.builder()
                                .inviteCode(expiredInviteCode)
                                .build();

                mockMvc.perform(post("/meetings/users")
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.EXPIRED_CODE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.EXPIRED_CODE.getMessage())))

                        .andDo(document("user-create-error-expired-code",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("inviteCode").description("모임의 초대 코드")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }


            @Test
            @DisplayName("해당 코드를 가진 모임이 없는 경우 예외가 발생한다.")
            public void 예외_없는초대코드() throws Exception {

                String nonexistentCode = "CCCCCC";
                Long unJoinedUserId = 10L;
                String unJoinedUserToken = createToken(unJoinedUserId);

                // 서비스 유효기간 예외 시나리오 모킹
                MeetingUserAddDto meetingUserAddDto = MeetingUserAddDto.builder()
                        .inviteCode(nonexistentCode)
                        .userId(unJoinedUserId)
                        .build();

                willThrow(new CustomException("해당 코드를 가진 모임이 없음", ErrorCode.NONEXISTENT_CODE))
                        .given(meetingUserService).add(refEq(meetingUserAddDto));

                MeetingUserAddRequest meetingUserAddRequest =
                        MeetingUserAddRequest.builder()
                                .inviteCode(nonexistentCode)
                                .build();

                mockMvc.perform(post("/meetings/users")
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.NONEXISTENT_CODE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.NONEXISTENT_CODE.getMessage())))

                        .andDo(document("user-create-error-nonexistent-code",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("inviteCode").description("모임의 초대 코드")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }


            @Test
            @DisplayName("이미 모임에 가입된 회원인 경우 예외가 발생한다.")
            public void 예외_이미가입된회원() throws Exception {

                String unexpiredInviteCode = "AAAAAA";
                Long joinedUserId = 20L;
                String joinedUserToken = createToken(joinedUserId);

                // 서비스 유효기간 예외 시나리오 모킹
                MeetingUserAddDto meetingUserAddDto = MeetingUserAddDto.builder()
                        .inviteCode(unexpiredInviteCode)
                        .userId(joinedUserId)
                        .build();

                willThrow(new CustomException("이미 회원이 가입한 모임임", ErrorCode.USER_ALREADY_PARTICIPATE))
                        .given(meetingUserService).add(refEq(meetingUserAddDto));

                MeetingUserAddRequest meetingUserAddRequest =
                        MeetingUserAddRequest.builder()
                                .inviteCode(unexpiredInviteCode)
                                .build();

                mockMvc.perform(post("/meetings/users")
                                .header("Authorization", joinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.USER_ALREADY_PARTICIPATE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.USER_ALREADY_PARTICIPATE.getMessage())))

                        .andDo(document("user-create-error-already-participate",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("inviteCode").description("모임의 초대 코드")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임 초대 코드가 요청 데이터에 없거나 형식에 맞지 않으면 예외가 발생한다.")
            public void 예외_필수데이터() throws Exception {

                String invalidInviteCode = "AAA";
                Long unJoinedUserId = 20L;
                String unJoinedUserToken = createToken(unJoinedUserId);

                MeetingUserAddRequest meetingUserAddRequest =
                        MeetingUserAddRequest.builder()
                                .inviteCode(invalidInviteCode)
                                .build();

                mockMvc.perform(post("/meetings/users")
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))

                        .andDo(document("user-create-error-param",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("inviteCode").description("모임의 초대 코드")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                        fieldWithPath("message.inviteCode").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                                ))
                        )
                ;
            }
        }
    }

}