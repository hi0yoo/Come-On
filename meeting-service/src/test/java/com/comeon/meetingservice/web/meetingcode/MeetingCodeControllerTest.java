package com.comeon.meetingservice.web.meetingcode;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingcode.dto.MeetingCodeModifyDto;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.meetingcode.response.MeetingCodeDetailResponse;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeetingCodeControllerTest extends ControllerTestBase {

    @Nested
    @DisplayName("모임코드 수정")
    class 모임코드수정 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적인 모임 코드를 수정하려고 할 경우 OK를 응답한다.")
            public void 정상흐름() throws Exception {
                // 10번 식별자를 가진 모임 코드는 코드 만료기간이 지난 갱신 가능한 코드라고 가정하여 결과를 모킹함
                Long normalCodeId = 10L;

                MeetingCodeModifyDto normalDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(normalCodeId)
                        .build();

                willDoNothing().given(meetingCodeService).modify(refEq(normalDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, normalCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("code-modify-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ))
                        )
                ;
            }

        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("만료기간이 남아있는 코드를 수정할 경우 Bad Request를 응답한다.")
            public void 예외_만료기간() throws Exception {
                // 20번 식별자를 가진 모임 코드는 코드 만료기간이 지나지 않은 갱신 불가능한 코드라고 가정하여 결과를 모킹함
                Long unexpiredCodeId = 20L;

                MeetingCodeModifyDto unexpiredDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(unexpiredCodeId)
                        .build();

                // refEq = equals() 오버라이딩 안해도 리플렉션을 통해 값을 비교함, eq = equals() 오버라이딩 안되어 있으면 실제 객체 주소비교해버림
                willThrow(new CustomException("만료 예외", ErrorCode.UNEXPIRED_CODE))
                        .given(meetingCodeService).modify(refEq(unexpiredDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, unexpiredCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.UNEXPIRED_CODE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.UNEXPIRED_CODE.getMessage())))

                        .andDo(document("code-modify-error-unexpired",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임 코드를 수정할 경우 Not Found를 응답한다.")
            public void 예외_코드식별자() throws Exception {
                // 30번 식별자를 가진 모임 코드는 없는 것으로 가정하여 결과를 모킹함
                Long notExistCodeId = 30L;
                
                MeetingCodeModifyDto notExistDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(notExistCodeId)
                        .build();
                
                willThrow(new CustomException("만료 예외", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingCodeService).modify(refEq(notExistDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, notExistCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-modify-error-code-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임일 경우 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {
                Long normalCodeId = 10L;

                MeetingCodeModifyDto normalDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .id(normalCodeId)
                        .build();

                willDoNothing().given(meetingCodeService).modify(refEq(normalDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedNonexistentMeetingId, normalCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-modify-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("회원이 모임에 미가입된 경우 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {
                Long normalCodeId = 10L;

                MeetingCodeModifyDto normalDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(normalCodeId)
                        .build();

                willDoNothing().given(meetingCodeService).modify(refEq(normalDto));

                String unJoinedUserToken = createToken(100L);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, normalCodeId)
                                .header("Authorization", unJoinedUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("code-modify-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("회원이 호스트가 아닌 경우 Forbidden 을 응답한다.")
            public void 예외_회원권한() throws Exception {
                Long normalCodeId = 10L;

                MeetingCodeModifyDto normalDto = MeetingCodeModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(normalCodeId)
                        .build();

                willDoNothing().given(meetingCodeService).modify(refEq(normalDto));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, normalCodeId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("code-modify-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }
        }
    }

    @Nested
    @DisplayName("모임코드 조회 - 단건")
    class 모임코드단건조회 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적으로 진행될 경우 OK를 응답한다.")
            public void 정상흐름() throws Exception {

                Long existentCodeId = 10L;
                String returnInviteCode = "DEC13A";
                Boolean returnIsExpired = false;

                MeetingCodeDetailResponse returnResponse = MeetingCodeDetailResponse.builder()
                        .id(existentCodeId)
                        .inviteCode(returnInviteCode)
                        .isExpired(returnIsExpired)
                        .build();

                given(meetingCodeQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentCodeId)))
                        .willReturn(returnResponse);

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(get("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, existentCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data.id", equalTo(existentCodeId), Long.class))
                        .andExpect(jsonPath("$.data.inviteCode", equalTo(returnInviteCode)))
                        .andExpect(jsonPath("$.data.isExpired", equalTo(returnIsExpired)))

                        .andDo(document("code-detail-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("조회하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("조회된 모임 코드의 ID"),
                                        fieldWithPath("inviteCode").type(JsonFieldType.STRING).description("모임 코드").attributes(key("format").value("[영문 대문자], [숫자], [영문 대문자 + 숫자 조합] 문자열 6자리")),
                                        fieldWithPath("isExpired").type(JsonFieldType.BOOLEAN).description("만료 여부")
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("없는 모임에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                Long existentCodeId = 10L;

                willThrow(new CustomException("해당 ID와 일치하는 모임이 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingCodeQueryService).getDetail(eq(mockedNonexistentMeetingId), eq(existentCodeId));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(get("/meetings/{meetingId}/codes/{codeId}", mockedNonexistentMeetingId, existentCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-detail-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("조회하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임 코드에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_코드식별자() throws Exception {

                Long nonexistentCodeId = 20L;

                willThrow(new CustomException("해당 ID와 일치하는 코드가 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingCodeQueryService).getDetail(eq(mockedExistentMeetingId), eq(nonexistentCodeId));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(get("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, nonexistentCodeId)
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-detail-error-code-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("조회하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임에 가입되지 않은 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                Long existentCodeId = 10L;
                String returnInviteCode = "DEC13A";
                Boolean returnIsExpired = false;

                MeetingCodeDetailResponse returnResponse = MeetingCodeDetailResponse.builder()
                        .id(existentCodeId)
                        .inviteCode(returnInviteCode)
                        .isExpired(returnIsExpired)
                        .build();

                given(meetingCodeQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentCodeId)))
                        .willReturn(returnResponse);

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(get("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, existentCodeId)
                                .header("Authorization", unJoinedUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("code-detail-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("조회하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST권한이 없는 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Long existentCodeId = 10L;
                String returnInviteCode = "DEC13A";
                Boolean returnIsExpired = false;

                MeetingCodeDetailResponse returnResponse = MeetingCodeDetailResponse.builder()
                        .id(existentCodeId)
                        .inviteCode(returnInviteCode)
                        .isExpired(returnIsExpired)
                        .build();

                given(meetingCodeQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentCodeId)))
                        .willReturn(returnResponse);

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}/codes/{codeId}", mockedExistentMeetingId, existentCodeId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.errorCode", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("code-detail-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("조회하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

        }

    }
}