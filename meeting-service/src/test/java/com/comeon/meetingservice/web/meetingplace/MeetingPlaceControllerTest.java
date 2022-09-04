package com.comeon.meetingservice.web.meetingplace;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceRemoveDto;
import com.comeon.meetingservice.domain.meetingplace.service.MeetingPlaceService;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.meetingplace.query.MeetingPlaceQueryService;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceModifyRequest;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceAddRequest;
import com.comeon.meetingservice.web.meetingplace.request.PlaceModifyRequestValidator;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.comeon.meetingservice.common.exception.ErrorCode.ENTITY_NOT_FOUND;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MeetingPlaceController.class)
@Import(PlaceModifyRequestValidator.class)
class MeetingPlaceControllerTest extends ControllerTestBase {

    @MockBean
    MeetingPlaceService meetingPlaceService;

    @MockBean
    MeetingPlaceQueryService meetingPlaceQueryService;

    @Nested
    @DisplayName("모임장소 저장")
    class 모임장소저장 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모든 필수 데이터가 넘어온 경우 Created와 저장된 ID를 응답한다.")
            public void 정상_흐름() throws Exception {

                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";

                MeetingPlaceAddDto normalDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .build();

                Long createPlaceId = 10L;

                given(meetingPlaceService.add(refEq(normalDto))).willReturn(createPlaceId);

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(post("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceAddRequest))
                        )

                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data", equalTo(createPlaceId), Long.class))

                        .andDo(document("place-create-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 장소를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도")
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

                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";

                MeetingPlaceAddDto nonexistentDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 모임을 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).add(refEq(nonexistentDto));

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(post("/meetings/{meetingId}/places", mockedNonexistentMeetingId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceAddRequest))
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))


                        .andDo(document("place-create-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 장소를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("필수 데이터를 보내지 않을 경우 Bad Request를 응답한다.")
            public void 예외_필수데이터() throws Exception {

                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";

                MeetingPlaceAddDto nonexistentDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 모임을 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).add(refEq(nonexistentDto));

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lng(addedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(post("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceAddRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))

                        .andDo(document("place-create-error-param",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 장소를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                        fieldWithPath("message.lat").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임에 가입되지 않은 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";

                MeetingPlaceAddDto nonexistentDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 모임을 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).add(refEq(nonexistentDto));

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .build();

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(post("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceAddRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("place-create-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 장소를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST, EDITOR가 아닌 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";

                MeetingPlaceAddDto nonexistentDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 모임을 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).add(refEq(nonexistentDto));

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(post("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceAddRequest))
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("place-create-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("모임 장소를 저장하려는 모임의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도")
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
    @DisplayName("모임장소 수정")
    class 모임장소수정 {

        @Nested
        @DisplayName("정상 흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모임 장소 정보를 수정할 경우 name, lat, lng 필드가 있다면 OK를 응답한다.")
            public void 정상_장소정보() throws Exception {

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("place-modify-normal-info",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("장소 메모를 수정할 경우 memo 필드만 있으면 OK를 응답한다.")
            public void 정상_장소메모() throws Exception {

                String modifiedMemo = "memo";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyMemoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .memo(modifiedMemo)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyMemoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .memo(modifiedMemo)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("place-modify-normal-memo",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모"),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("장소 순서를 수정할 경우 order 필드만 있으면 OK를 응답한다.")
            public void 모임_장소_순서() throws Exception {

                Integer modifiedOrder = 5;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyOrderDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .order(modifiedOrder)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyOrderDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .order(modifiedOrder)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("place-modify-normal-order",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서")
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("모임 장소를 수정할 경우 name, lat, lng 필드 중 하나라도 없다면 Bad Request를 응답한다.")
            public void 예외_장소정보필수데이터() throws Exception {

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto invalidModifyInfoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(invalidModifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lng(modifiedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))

                        .andDo(document("place-modify-error-info",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                        fieldWithPath("message.objectError").type(JsonFieldType.ARRAY).description("검증이 실패한 이유")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임장소에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_장소식별자() throws Exception {

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long nonexistentPlaceId = 20L;

                MeetingPlaceModifyDto nonexistentDto = MeetingPlaceModifyDto.builder()
                        .id(nonexistentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 장소를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).modify(refEq(nonexistentDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, nonexistentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-modify-error-place-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
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

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .build();

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedNonexistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-modify-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임에 가입된 회원이 아니라면 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .build();

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", unJoinedUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("place-modify-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST, EDITOR가 아닌 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .id(existentPlaceId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .build();

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(patch("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", participantUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("place-modify-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("수정하려는 모임 장소의 ID")
                                ),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모").optional(),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
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
    @DisplayName("모임장소 삭제")
    class 모임장소삭제 {


        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("요청 회원이 HOST이고 모든 리소스가 있다면 OK를 응답한다.")
            public void 정상_흐름() throws Exception {
                Long existentPlaceId = 10L;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto
                        .builder()
                        .id(existentPlaceId)
                        .build();

                willDoNothing().given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(delete("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("place-delete-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("삭제하려는 모임 장소의 ID")
                                ))
                        )
                ;
            }

        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("없는 모임장소에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_장소식별자() throws Exception {

                Long nonexistentPlaceId = 20L;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto
                        .builder()
                        .id(nonexistentPlaceId)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 장소를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(delete("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, nonexistentPlaceId)
                                .header("Authorization", editorUserToken)
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-delete-error-place-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("삭제하려는 모임 장소의 ID")
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

                Long existentPlaceId = 10L;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto
                        .builder()
                        .id(existentPlaceId)
                        .build();

                willDoNothing().given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(delete("/meetings/{meetingId}/places/{placeId}", mockedNonexistentMeetingId, existentPlaceId)
                                .header("Authorization", editorUserToken)
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-delete-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("삭제하려는 모임 장소의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임에 가입되지 않은 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                Long existentPlaceId = 10L;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto
                        .builder()
                        .id(existentPlaceId)
                        .build();

                willDoNothing().given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(delete("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", unJoinedUserToken)
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("place-delete-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("삭제하려는 모임 장소의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST, EDITOR가 아닌 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Long existentPlaceId = 10L;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto
                        .builder()
                        .id(existentPlaceId)
                        .build();

                willDoNothing().given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(delete("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", participantUserToken)
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("place-delete-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 EDITOR, HOST 인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("삭제하려는 모임 장소의 ID")
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

