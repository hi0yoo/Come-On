package com.comeon.meetingservice.web.meetingdate;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.service.MeetingDateService;
import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.meetingdate.query.MeetingDateQueryService;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateAddRequest;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateModifyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingDateController.class)
class MeetingDateControllerTest extends ControllerTestBase {

    @MockBean
    MeetingDateService meetingDateService;

    @MockBean
    MeetingDateQueryService meetingDateQueryService;

    @Nested
    @DisplayName("모임날짜 저장")
    class 모임날짜저장 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모든 필수 데이터가 넘어온 경우 Created코드와 저장된, 혹은 영향을 받은 ID를 응답한다.")
            public void 정상_흐름() throws Exception {

                LocalDate addedDate = LocalDate.of(2022, 06, 10);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(addedDate)
                        .build();

                Long createdDateId = 10L;
                given(meetingDateService.add(refEq(meetingDateAddDto))).willReturn(createdDateId);

                MeetingDateAddRequest meetingDateAddRequest =
                        MeetingDateAddRequest.builder()
                                .date(addedDate)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateAddRequest))
                        )

                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data", equalTo(createdDateId), Long.class))

                        .andDo(document("date-create-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(key("format").value("yyyy-MM-dd"))
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("날짜가 모임 기간내에 없다면 Bad Request를 응답한다.")
            public void 예외_날짜기간내미포함() throws Exception {

                LocalDate notWithinPeriodDate = LocalDate.of(2022, 05, 10);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(notWithinPeriodDate)
                        .build();

                willThrow(new CustomException("날짜가 모임 기간 내에 포함되지 않음", ErrorCode.DATE_NOT_WITHIN_PERIOD))
                        .given(meetingDateService).add(refEq(meetingDateAddDto));

                MeetingDateAddRequest meetingDateAddRequest =
                        MeetingDateAddRequest.builder()
                                .date(notWithinPeriodDate)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.DATE_NOT_WITHIN_PERIOD.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.DATE_NOT_WITHIN_PERIOD.getMessage())))

                        .andDo(document("date-create-error-not-within",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(key("format").value("yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }


            @Test
            @DisplayName("이미 해당 회원이 해당 날짜를 선택했다면 Bad Request를 응답한다.")
            public void 예외_이미선택한날짜() throws Exception {

                LocalDate alreadySelectedDate = LocalDate.of(2022, 06, 30);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(alreadySelectedDate)
                        .build();


                willThrow(new CustomException("이미 회원이 선택한 날짜", ErrorCode.USER_ALREADY_SELECT))
                        .given(meetingDateService).add(refEq(meetingDateAddDto));

                MeetingDateAddRequest meetingDateAddRequest =
                        MeetingDateAddRequest.builder()
                                .date(alreadySelectedDate)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.USER_ALREADY_SELECT.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.USER_ALREADY_SELECT.getMessage())))

                        .andDo(document("date-create-error-already-select",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }


            @Test
            @DisplayName("필수 데이터가 없거나 형식이 틀리다면 Bad Request를 응답한다.")
            public void 예외_필수값() throws Exception {

                LocalDate addedDate = LocalDate.of(2022, 06, 10);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(addedDate)
                        .build();

                Long createdDateId = 10L;
                given(meetingDateService.add(refEq(meetingDateAddDto))).willReturn(createdDateId);

                Map<String, String> invalidFormatRequest = new HashMap<>();
                invalidFormatRequest.put("date", "123456");

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(invalidFormatRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getCode())))

                        .andDo(document("date-create-error-param",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                LocalDate addedDate = LocalDate.of(2022, 06, 10);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(addedDate)
                        .build();

                Long createdDateId = 10L;
                given(meetingDateService.add(refEq(meetingDateAddDto))).willReturn(createdDateId);

                MeetingDateAddRequest meetingDateAddRequest =
                        MeetingDateAddRequest.builder()
                                .date(addedDate)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedNonexistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateAddRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("date-create-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("해당 회원이 모임에 가입되어있지 않다면 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                LocalDate addedDate = LocalDate.of(2022, 06, 10);

                MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .date(addedDate)
                        .build();

                Long createdDateId = 10L;
                given(meetingDateService.add(refEq(meetingDateAddDto))).willReturn(createdDateId);

                MeetingDateAddRequest meetingDateAddRequest =
                        MeetingDateAddRequest.builder()
                                .date(addedDate)
                                .build();

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(post("/meetings/{meetingId}/dates", mockedExistentMeetingId)
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateAddRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("date-create-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 날짜를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
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

    @Nested
    @DisplayName("모임날짜 수정")
    class 모임날짜수정 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {


            @Test
            @DisplayName("모든 필수 데이터가 넘어오고 형식이 맞다면 OK를 응답한다.")
            public void 정상_흐름() throws Exception {

                Long existentPlaceId = 10L;
                DateStatus modifiedStatus = DateStatus.FIXED;

                MeetingDateModifyDto normalDto = MeetingDateModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .dateStatus(modifiedStatus)
                        .build();

                willDoNothing().given(meetingDateService).modify(refEq(normalDto));

                MeetingDateModifyRequest meetingDateModifyRequest =
                        MeetingDateModifyRequest.builder()
                                .dateStatus(modifiedStatus)
                                .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateModifyRequest))
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("date-modify-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 날짜가 속한 모임의 ID"),
                                        parameterWithName("dateId").description("수정하려는 모임 날짜의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(key("format").value("FIXED, UNFIXED"))
                                ))
                        )
                ;
            }

        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("DateStatus 형식에 맞지 않으면 Bad Request를 응답한다.")
            public void 형식_예외() throws Exception {

                Long existentPlaceId = 10L;
                DateStatus modifiedStatus = DateStatus.FIXED;

                MeetingDateModifyDto normalDto = MeetingDateModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .dateStatus(modifiedStatus)
                        .build();

                willDoNothing().given(meetingDateService).modify(refEq(normalDto));

                Map<String, String> invalidFormatRequest = new HashMap<>();
                invalidFormatRequest.put("dateStatus", "xxx");

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(invalidFormatRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage())))

                        .andDo(document("date-modify-error-format",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 날짜가 속한 모임의 ID"),
                                        parameterWithName("dateId").description("수정하려는 모임 날짜의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(key("format").value("FIXED, UNFIXED"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                Long nonExistentPlaceId = 20L;
                DateStatus modifiedStatus = DateStatus.FIXED;

                MeetingDateModifyDto nonExistentDto = MeetingDateModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(nonExistentPlaceId)
                        .dateStatus(modifiedStatus)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 날짜가 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingDateService).modify(refEq(nonExistentDto));

                MeetingDateModifyRequest meetingDateModifyRequest =
                        MeetingDateModifyRequest.builder()
                                .dateStatus(modifiedStatus)
                                .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", mockedExistentMeetingId, nonExistentPlaceId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateModifyRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("date-modify-error-date-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 날짜가 속한 모임의 ID"),
                                        parameterWithName("dateId").description("수정하려는 모임 날짜의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(key("format").value("FIXED, UNFIXED"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 날짜에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_날짜식별자() throws Exception {

                Long nonExistentPlaceId = 20L;
                DateStatus modifiedStatus = DateStatus.FIXED;

                MeetingDateModifyDto nonExistentDto = MeetingDateModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(nonExistentPlaceId)
                        .dateStatus(modifiedStatus)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 날짜가 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingDateService).modify(refEq(nonExistentDto));

                MeetingDateModifyRequest meetingDateModifyRequest =
                        MeetingDateModifyRequest.builder()
                                .dateStatus(modifiedStatus)
                                .build();

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", mockedExistentMeetingId, nonExistentPlaceId)
                                .header("Authorization", hostUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateModifyRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("date-modify-error-date-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 날짜가 속한 모임의 ID"),
                                        parameterWithName("dateId").description("수정하려는 모임 날짜의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(key("format").value("FIXED, UNFIXED"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST 권한이 없는 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Long existentPlaceId = 10L;
                DateStatus modifiedStatus = DateStatus.FIXED;

                MeetingDateModifyDto normalDto = MeetingDateModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .dateStatus(modifiedStatus)
                        .build();

                willDoNothing().given(meetingDateService).modify(refEq(normalDto));

                MeetingDateModifyRequest meetingDateModifyRequest =
                        MeetingDateModifyRequest.builder()
                                .dateStatus(modifiedStatus)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingDateModifyRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("date-modify-error-date-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 날짜가 속한 모임의 ID"),
                                        parameterWithName("dateId").description("수정하려는 모임 날짜의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(key("format").value("FIXED, UNFIXED"))
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

//
//    @Nested
//    @DisplayName("모임날짜 삭제")
//    class 모임날짜삭제{
//
//        @Test
//        @DisplayName("경로변수와 회원ID 값이 유효하다면 정상적으로 삭제된다.")
//        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//        public void 정상_흐름() throws Exception {
//
//            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 10)
//                            .header("Authorization", selectedToken)
//                            .contentType(MediaType.APPLICATION_JSON)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andDo(document("date-delete-normal",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint())
//                    ))
//            ;
//        }
//
//        @Test
//        @DisplayName("회원이 해당 날짜를 선택하지 않은 경우 BadRequest와 예외 정보를 응답한다.")
//        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//        public void 회원_예외() throws Exception {
//
//            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 10)
//                            .header("Authorization", sampleToken)
//                            .contentType(MediaType.APPLICATION_JSON)
//                    )
//                    .andExpect(status().isBadRequest())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.USER_NOT_SELECT_DATE.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.USER_NOT_SELECT_DATE.getMessage())))
//                    .andDo(document("date-delete-error-user",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            responseFields(beneathPath("data").withSubsectionId("data"),
//                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
//                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
//                            ))
//                    )
//            ;
//        }
//
//        @Test
//        @DisplayName("없는 날짜 ID로 요청을 보낼 경우 NotFound와 예외 정보를 응답한다.")
//        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//        public void 식별자_예외() throws Exception {
//
//            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 5)
//                            .header("Authorization", sampleToken)
//                            .contentType(MediaType.APPLICATION_JSON)
//                    )
//                    .andExpect(status().isNotFound())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
//                    .andDo(document("date-delete-error-pathvariable",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            responseFields(beneathPath("data").withSubsectionId("data"),
//                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
//                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
//                            ))
//                    )
//            ;
//        }
//    }
//
//    @Nested
//    @DisplayName("모임날짜조회 - 단건")
//    class 모임날짜단건조회 {
//
//        @Test
//        @DisplayName("날짜 ID가 정상적이라면 날짜와 날짜를 선택한 회원 정보들을 응답한다.")
//        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//        public void 정상_흐름() throws Exception {
//
//            mockMvc.perform(get("/meetings/{meetingId}/dates/{dateId}", 10, 10)
//                            .header("Authorization", selectedToken)
//                            .contentType(MediaType.APPLICATION_JSON)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andDo(document("date-detail-normal",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            responseFields(beneathPath("data").withSubsectionId("data"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("조회된 모임 날짜의 ID"),
//                                    fieldWithPath("date").type(JsonFieldType.STRING).description("모임 날짜의 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd")),
//                                    fieldWithPath("userCount").type(JsonFieldType.NUMBER).description("해당 날짜를 선택한 회원 수"),
//                                    fieldWithPath("dateStatus").type(JsonFieldType.STRING).description("해당 날짜의 확정 여부").attributes(new Attributes.Attribute("format", "FIXED, UNFIXED")),
//                                    subsectionWithPath("dateUsers").type(JsonFieldType.ARRAY).description("해당 날짜를 선택한 회원들의 정보")
//                            ),
//                            responseFields(beneathPath("data.dateUsers.[]").withSubsectionId("date-users"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("해당 날짜를 선택한 회원의 모임 회원 ID"),
//                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("해당 모임 회원의 닉네임"),
//                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("해당 모임 회원의 프로필 이미지 링크"),
//                                    fieldWithPath("meetingRole").type(JsonFieldType.STRING).description("해당 모임 회원의 역할").attributes(key("format").value("HOST, PARTICIPANT"))
//                            ))
//                    )
//            ;
//        }
//
//        @Test
//        @DisplayName("없는 모임날짜 리소스를 조회하려고 할 경우 NotFound와 예외 정보를 응답한다.")
//        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//        public void 경로변수_예외() throws Exception {
//
//            mockMvc.perform(get("/meetings/{meetingId}/dates/{dateId}", 10, 5)
//                            .header("Authorization", sampleToken)
//                    )
//                    .andExpect(status().isNotFound())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
//                    .andDo(document("date-detail-error-pathvariable",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            responseFields(beneathPath("data").withSubsectionId("data"),
//                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
//                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
//                            ))
//                    )
//            ;
//        }
//    }
//}