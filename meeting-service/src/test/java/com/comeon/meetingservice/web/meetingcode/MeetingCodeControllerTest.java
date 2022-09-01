package com.comeon.meetingservice.web.meetingcode;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingcode.dto.MeetingCodeModifyDto;
import com.comeon.meetingservice.domain.meetingcode.service.MeetingCodeService;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.util.TokenUtils;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingCodeController.class)
class MeetingCodeControllerTest extends ControllerTestBase {

    @MockBean
    MeetingCodeService meetingCodeService;

    @MockBean
    MeetingUserQueryRepository meetingUserQueryRepository;

    @Autowired
    MeetingCodeController meetingCodeController;

    @Nested
    @DisplayName("모임코드 수정")
    class 모임코드수정 {

        Long normalUserId;
        String normalToken;

        Long notJoinedUserId;
        String notJoinedToken;

        Long notHostUserId;
        String notHostToken;

        Long existMeetingId;
        Long notExistMeetingId;

        MockedStatic<TokenUtils> tokenUtilsMock;

        @BeforeEach
        public void initInterceptorMocking() {

            // 일반 유저, HOST 이면서 모임에 가입된 유저
            normalUserId = 1L;
            normalToken = createToken(normalUserId);

            // 모임에 가입되지 않은 유저
            notJoinedUserId = 2L;
            notJoinedToken = createToken(notJoinedUserId);

            // 모임에 가입되었지만 HOST가 아닌 유저
            notHostUserId = 3L;
            notHostToken = createToken(notHostUserId);

            // TokenUtils의 getUserId 스태틱 메서드 모킹
            tokenUtilsMock = Mockito.mockStatic(TokenUtils.class);
            given(TokenUtils.getUserId(normalToken)).willReturn(normalUserId);
            given(TokenUtils.getUserId(notJoinedToken)).willReturn(notJoinedUserId);
            given(TokenUtils.getUserId(notHostToken)).willReturn(notHostUserId);

            List<MeetingUserEntity> meetingUserEntities = new ArrayList<>();
            MeetingUserEntity hostUser = MeetingUserEntity.builder()
                    .userId(normalUserId)
                    .meetingRole(MeetingRole.HOST)
                    .build();

            MeetingUserEntity participantUser = MeetingUserEntity.builder()
                    .userId(notHostUserId)
                    .meetingRole(MeetingRole.PARTICIPANT)
                    .build();

            meetingUserEntities.add(hostUser);
            meetingUserEntities.add(participantUser);

            // 실제 존재하는 모임과 존재하지 않는 모임 모킹
            existMeetingId = 10L;
            notExistMeetingId = 5L;
            given(meetingUserQueryRepository.findAllByMeetingId(existMeetingId)).willReturn(meetingUserEntities);
            given(meetingUserQueryRepository.findAllByMeetingId(notExistMeetingId)).willReturn(new ArrayList<>());

        }

        @AfterEach
        public void closeMockedStatic() {
            tokenUtilsMock.close();
        }

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적인 모임 코드를 수정하려고 할 경우 요청이 정상 처리된다.")
            public void 정상흐름() throws Exception {
                // 10번 식별자를 가진 모임 코드는 코드 만료기간이 지난 갱신 가능한 코드라고 가정하여 결과를 모킹함
                Long normalCodeId = 10L;
                MeetingCodeModifyDto normalDto = MeetingCodeModifyDto.builder().id(normalCodeId).build();
                willDoNothing().given(meetingCodeService).modify(eq(normalDto));

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", existMeetingId, normalCodeId)
                                .header("Authorization", normalToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("code-modify-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
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
            @DisplayName("만료기간이 남아있는 코드를 수정할 경우 예외가 발생한다.")
            public void 예외_만료기간() throws Exception {
                // 20번 식별자를 가진 모임 코드는 코드 만료기간이 지나지 않은 갱신 불가능한 코드라고 가정하여 결과를 모킹함
                Long unexpiredCodeId = 20L;
                MeetingCodeModifyDto unexpiredDto = MeetingCodeModifyDto.builder().id(unexpiredCodeId).build();

                // refEq = equals() 오버라이딩 안해도 리플렉션을 통해 값을 비교함, eq = equals() 오버라이딩 안되어 있으면 실제 객체 주소비교해버림
                willThrow(new CustomException("만료 예외", ErrorCode.UNEXPIRED_CODE))
                        .given(meetingCodeService).modify(refEq(unexpiredDto));

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", existMeetingId, unexpiredCodeId)
                                .header("Authorization", normalToken)
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.UNEXPIRED_CODE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.UNEXPIRED_CODE.getMessage())))

                        .andDo(document("code-modify-error-unexpired",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임 코드를 수정할 경우 NotFound 를 응답한다.")
            public void 예외_코드식별자() throws Exception {
                // 30번 식별자를 가진 모임 코드는 없는 것으로 가정하여 결과를 모킹함
                Long notExistCodeId = 30L;
                MeetingCodeModifyDto notExistDto = MeetingCodeModifyDto.builder().id(notExistCodeId).build();
                willThrow(new CustomException("만료 예외", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingCodeService).modify(refEq(notExistDto));

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", existMeetingId, notExistCodeId)
                                .header("Authorization", normalToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-modify-error-code-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임일 경우 NotFound 를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", notExistMeetingId, 10L)
                                .header("Authorization", normalToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("code-modify-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("회원이 모임에 미가입된 경우 ForBidden 을 응답한다.")
            public void 예외_회원미가입() throws Exception {
                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", existMeetingId, 10L)
                                .header("Authorization", notJoinedToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("code-modify-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰")
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("회원이 호스트가 아닌 경우 ForBidden 을 응답한다.")
            public void 예외_회원호스트() throws Exception {

                mockMvc.perform(patch("/meetings/{meetingId}/codes/{codeId}", existMeetingId, 10L)
                                .header("Authorization", notHostToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", Matchers.equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_HOST.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_HOST.getMessage())))

                        .andDo(document("code-modify-error-not-host",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임 코드가 속해있는 모임의 ID"),
                                        parameterWithName("codeId").description("수정하려는 모임 코드의 ID")
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