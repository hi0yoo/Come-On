package com.comeon.meetingservice.web.meetinguser;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserModifyDto;
import com.comeon.meetingservice.domain.meetinguser.service.MeetingUserService;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserAddRequest;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserModifyRequest;
import com.comeon.meetingservice.web.meetinguser.response.MeetingUserAddResponse;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class MeetingUserControllerTest extends ControllerTestBase {

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

                Long meetingId = 15L;
                Long createdMeetingUserId = 20L;
                given(meetingUserService.add(refEq(meetingUserAddDto))).willReturn(createdMeetingUserId);
                given(meetingUserQueryService.getMeetingIdAndUserId(createdMeetingUserId))
                        .willReturn(
                                MeetingUserAddResponse.builder()
                                        .meetingId(meetingId)
                                        .meetingUserId(createdMeetingUserId)
                                        .build()
                        );

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
                        .andExpect(jsonPath("$.data.meetingId", equalTo(meetingId), Long.class))
                        .andExpect(jsonPath("$.data.meetingUserId", equalTo(createdMeetingUserId), Long.class))

                        .andDo(document("user-create-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
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
            @DisplayName("유효기간이 지난 코드인 경우Bad Request를 응답한다.")
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
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
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
            @DisplayName("해당 코드를 가진 모임이 없는 경우Bad Request를 응답한다.")
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
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
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
            @DisplayName("이미 모임에 가입된 회원인 경우Bad Request를 응답한다.")
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
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
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
            @DisplayName("모임 초대 코드가 요청 데이터에 없거나 형식에 맞지 않으면Bad Request를 응답한다.")
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
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
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

    @Nested
    @DisplayName("모임유저 수정")
    class 모임유저수정 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모임 ID와 모임 유저 ID가 정상이라면 OK를 응답한다.")
            public void 정상흐름() throws Exception {

                MeetingRole modifyingRole = MeetingRole.EDITOR;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedParticipantUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 정상 시나리오 모킹 (정상 수정되면 리턴값 없음)
                willDoNothing().given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, mockedParticipantUserId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("user-modify-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("변경하려는 역할이 HOST일 경우Bad Request를 응답한다.")
            public void 예외_HOST로변경() throws Exception {

                MeetingRole modifyingRole = MeetingRole.HOST;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedParticipantUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willThrow(new CustomException("HOST로 변경을 지원하지 않음", ErrorCode.MODIFY_HOST_NOT_SUPPORT))
                        .given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, mockedParticipantUserId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MODIFY_HOST_NOT_SUPPORT.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MODIFY_HOST_NOT_SUPPORT.getMessage())))

                        .andDo(document("user-modify-error-modifying-host",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST의 역할을 변경할 경우Bad Request를 응답한다.")
            public void 예외_HOST를변경() throws Exception {

                MeetingRole modifyingRole = MeetingRole.PARTICIPANT;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedHostUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willThrow(new CustomException("HOST회원의 역할은 변경 불가능", ErrorCode.MODIFY_HOST_IMPOSSIBLE))
                        .given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, mockedHostUserId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MODIFY_HOST_IMPOSSIBLE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MODIFY_HOST_IMPOSSIBLE.getMessage())))

                        .andDo(document("user-modify-error-host-modified",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("해당 모임에 없는 회원이면Bad Request를 응답한다.")
            public void 예외_모임유저식별자() throws Exception {

                MeetingRole modifyingRole = MeetingRole.EDITOR;
                Long unJoinedUserId = 10L;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(unJoinedUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willThrow(new CustomException("모임 유저 엔티티를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, unJoinedUserId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("user-modify-error-user-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("요청을 보낸 모임 자체가 없다면 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                MeetingRole modifyingRole = MeetingRole.EDITOR;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedParticipantUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willDoNothing().given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedNonexistentMeetingId, mockedParticipantUserId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("user-modify-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("요청을 보낸 회원이 모임에 가입되어있지 않다면 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                MeetingRole modifyingRole = MeetingRole.EDITOR;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedParticipantUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willDoNothing().given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, mockedParticipantUserId)
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("user-modify-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("요청을 보낸 회원이 HOST가 아니라면 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                MeetingRole modifyingRole = MeetingRole.EDITOR;
                MeetingUserModifyDto meetingUserModifyDto = MeetingUserModifyDto.builder()
                        .id(mockedParticipantUserId)
                        .meetingId(mockedExistentMeetingId)
                        .meetingRole(modifyingRole)
                        .build();

                // 서비스 예외 시나리오 모킹
                willDoNothing().given(meetingUserService).modify(refEq(meetingUserModifyDto));

                // 요청 데이터
                MeetingUserModifyRequest meetingUserModifyRequest = MeetingUserModifyRequest.builder()
                        .meetingRole(modifyingRole)
                        .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/users/{userId}", mockedExistentMeetingId, mockedParticipantUserId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingUserModifyRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("user-modify-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 회원이 속해있는 모임의 ID"),
                                        parameterWithName("userId").description("수정하려는 모임 회원의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("meetingRole").description("수정할 회원의 역할").attributes(key("format").value("EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }
        }
    }

}