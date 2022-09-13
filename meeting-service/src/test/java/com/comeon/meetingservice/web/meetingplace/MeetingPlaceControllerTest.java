package com.comeon.meetingservice.web.meetingplace;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceRemoveDto;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.ListResponse;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceModifyRequest;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceAddRequest;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceDetailResponse;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.ArrayList;
import java.util.List;

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

class MeetingPlaceControllerTest extends ControllerTestBase {

    @Nested
    @DisplayName("모임장소 저장")
    class 모임장소저장 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모든 필수 데이터가 넘어온 경우 Created와 저장된 ID를 응답한다.")
            public void 정상_흐름() throws Exception {

                Long addedApiId = 500L;
                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";
                PlaceCategory addedCategory = PlaceCategory.BAR;
                String addedMemo = "memo";

                MeetingPlaceAddDto normalDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .apiId(addedApiId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .category(addedCategory)
                        .memo(addedMemo)
                        .build();

                Long createPlaceId = 10L;

                given(meetingPlaceService.add(refEq(normalDto))).willReturn(createPlaceId);

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .apiId(addedApiId)
                                .category(addedCategory)
                                .memo(addedMemo)
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
                                        fieldWithPath("apiId").description("추가할 장소의 카카오 API ID"),
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도"),
                                        fieldWithPath("category").description("추가할 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").description("추가할 장소의 메모").optional()
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

                Long addedApiId = 500L;
                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";
                PlaceCategory addedCategory = PlaceCategory.BAR;
                String addedMemo = "memo";

                MeetingPlaceAddDto nonexistentDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .apiId(addedApiId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .category(addedCategory)
                        .memo(addedMemo)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 모임을 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).add(refEq(nonexistentDto));

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .apiId(addedApiId)
                                .category(addedCategory)
                                .memo(addedMemo)
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
                                        fieldWithPath("apiId").description("추가할 장소의 카카오 API ID"),
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도"),
                                        fieldWithPath("category").description("추가할 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").description("추가할 장소의 메모").optional()
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

                Long addedApiId = 500L;
                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";
                PlaceCategory addedCategory = PlaceCategory.BAR;
                String addedMemo = "memo";

                MeetingPlaceAddDto normalDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .apiId(addedApiId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .category(addedCategory)
                        .memo(addedMemo)
                        .build();

                Long createPlaceId = 10L;

                given(meetingPlaceService.add(refEq(normalDto))).willReturn(createPlaceId);

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .apiId(addedApiId)
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
                                        fieldWithPath("apiId").description("추가할 장소의 카카오 API ID"),
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도"),
                                        fieldWithPath("category").description("추가할 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").description("추가할 장소의 메모").optional()
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                        fieldWithPath("message.category").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("모임에 가입되지 않은 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원미가입() throws Exception {

                Long addedApiId = 500L;
                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";
                PlaceCategory addedCategory = PlaceCategory.BAR;
                String addedMemo = "memo";

                MeetingPlaceAddDto normalDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .apiId(addedApiId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .category(addedCategory)
                        .memo(addedMemo)
                        .build();

                Long createPlaceId = 10L;

                given(meetingPlaceService.add(refEq(normalDto))).willReturn(createPlaceId);

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .apiId(addedApiId)
                                .category(addedCategory)
                                .memo(addedMemo)
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
                                        fieldWithPath("apiId").description("추가할 장소의 카카오 API ID"),
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도"),
                                        fieldWithPath("category").description("추가할 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").description("추가할 장소의 메모").optional()
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

                Long addedApiId = 500L;
                Double addedLat = 10.1;
                Double addedLng = 20.1;
                String addedName = "name";
                PlaceCategory addedCategory = PlaceCategory.BAR;
                String addedMemo = "memo";

                MeetingPlaceAddDto normalDto = MeetingPlaceAddDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .apiId(addedApiId)
                        .lat(addedLat)
                        .lng(addedLng)
                        .name(addedName)
                        .category(addedCategory)
                        .memo(addedMemo)
                        .build();

                Long createPlaceId = 10L;

                given(meetingPlaceService.add(refEq(normalDto))).willReturn(createPlaceId);

                MeetingPlaceAddRequest meetingPlaceAddRequest =
                        MeetingPlaceAddRequest.builder()
                                .name(addedName)
                                .lat(addedLat)
                                .lng(addedLng)
                                .apiId(addedApiId)
                                .category(addedCategory)
                                .memo(addedMemo)
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
                                        fieldWithPath("apiId").description("추가할 장소의 카카오 API ID"),
                                        fieldWithPath("name").description("추가할 장소의 이름"),
                                        fieldWithPath("lat").description("추가할 장소의 위도"),
                                        fieldWithPath("lng").description("추가할 장소의 경도"),
                                        fieldWithPath("category").description("추가할 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").description("추가할 장소의 메모").optional()
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
            @DisplayName("모임 장소 정보를 수정할 경우 apiId, name, lat, lng, category 필드가 있다면 OK를 응답한다.")
            public void 정상_장소정보() throws Exception {

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                PlaceCategory modifiedCategory = PlaceCategory.CAFE;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .apiId(modifiedApiId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .category(modifiedCategory)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .apiId(modifiedApiId)
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .category(modifiedCategory)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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
                        .meetingId(mockedExistentMeetingId)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID").optional(),
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)).optional(),
                                        fieldWithPath("memo").description("수정할 장소의 메모"),
                                        fieldWithPath("order").description("수정할 장소의 순서").optional()
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("장소 순서를 수정할 경우 order 필드만 있으면 OK를 응답한다.")
            public void 정상_장소순서() throws Exception {

                Integer modifiedOrder = 5;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyOrderDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID").optional(),
                                        fieldWithPath("name").description("수정할 장소의 이름").optional(),
                                        fieldWithPath("lat").description("수정할 장소의 위도").optional(),
                                        fieldWithPath("lng").description("수정할 장소의 경도").optional(),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)).optional(),
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
            @DisplayName("모임 장소 정보를 수정할 경우 apiId, name, lat, lng 필드 중 하나라도 없다면 Bad Request를 응답한다.")
            public void 예외_장소정보필수데이터() throws Exception {

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto invalidModifyInfoDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .apiId(modifiedApiId)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                PlaceCategory modifiedCategory = PlaceCategory.CAFE;
                Long nonexistentPlaceId = 20L;

                MeetingPlaceModifyDto nonexistentDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(nonexistentPlaceId)
                        .apiId(modifiedApiId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .category(modifiedCategory)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 장소를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).modify(refEq(nonexistentDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .apiId(modifiedApiId)
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .category(modifiedCategory)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                PlaceCategory modifiedCategory = PlaceCategory.CAFE;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .id(existentPlaceId)
                        .apiId(modifiedApiId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .category(modifiedCategory)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 리소스가 없음", ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .apiId(modifiedApiId)
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .category(modifiedCategory)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                PlaceCategory modifiedCategory = PlaceCategory.CAFE;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .apiId(modifiedApiId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .category(modifiedCategory)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .apiId(modifiedApiId)
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .category(modifiedCategory)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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

                Long modifiedApiId = 1000L;
                Double modifiedLat = 10.1;
                Double modifiedLng = 20.1;
                String modifiedName = "name";
                PlaceCategory modifiedCategory = PlaceCategory.CAFE;
                Long existentPlaceId = 10L;

                MeetingPlaceModifyDto modifyInfoDto = MeetingPlaceModifyDto.builder()
                        .meetingId(mockedExistentMeetingId)
                        .id(existentPlaceId)
                        .apiId(modifiedApiId)
                        .lat(modifiedLat)
                        .lng(modifiedLng)
                        .name(modifiedName)
                        .category(modifiedCategory)
                        .build();

                willDoNothing().given(meetingPlaceService).modify(refEq(modifyInfoDto));

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .apiId(modifiedApiId)
                                .name(modifiedName)
                                .lat(modifiedLat)
                                .lng(modifiedLng)
                                .category(modifiedCategory)
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
                                        fieldWithPath("apiId").description("수정할 장소의 카카오 장소 API ID"),
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
                                        fieldWithPath("category").description("수정할 장소의 카테고리").attributes(key("format").value(categoryLink)),
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
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto.builder()
                        .meetingId(mockedExistentMeetingId)
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
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto.builder()
                        .meetingId(mockedExistentMeetingId)
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
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto.builder()
                        .meetingId(mockedNonexistentMeetingId)
                        .id(existentPlaceId)
                        .build();

                willThrow(new CustomException("해당 ID와 일치하는 리소스를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceService).remove(refEq(meetingPlaceRemoveDto));

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
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto.builder()
                        .meetingId(mockedExistentMeetingId)
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
                MeetingPlaceRemoveDto meetingPlaceRemoveDto = MeetingPlaceRemoveDto.builder()
                        .meetingId(mockedExistentMeetingId)
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

    @Nested
    @DisplayName("모임장소 조회 - 단건")
    class 모임장소단건조회 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적으로 조회될 경우 OK와 장소 정보를 응답한다.")
            public void 정상_흐름() throws Exception {

                Long existentPlaceId = 10L;
                Long returnApiId = 1000L;
                PlaceCategory returnCategory = PlaceCategory.CAFE;
                String returnMemo = "memo";
                String returnName = "place name";
                Double returnLat = 10.1;
                Double returnLng = 20.1;

                MeetingPlaceDetailResponse returnResponse = MeetingPlaceDetailResponse.builder()
                        .id(existentPlaceId)
                        .apiId(returnApiId)
                        .category(returnCategory.getKorName())
                        .memo(returnMemo)
                        .name(returnName)
                        .lat(returnLat)
                        .lng(returnLng)
                        .build();

                given(meetingPlaceQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentPlaceId)))
                        .willReturn(returnResponse);

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", participantUserToken)
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data.name", equalTo(returnName)))
                        .andExpect(jsonPath("$.data.id", equalTo(existentPlaceId), Long.class))
                        .andExpect(jsonPath("$.data.apiId", equalTo(returnApiId), Long.class))
                        .andExpect(jsonPath("$.data.category", equalTo(returnCategory.getKorName())))
                        .andExpect(jsonPath("$.data.memo", equalTo(returnMemo)))
                        .andExpect(jsonPath("$.data.lat", equalTo(returnLat)))
                        .andExpect(jsonPath("$.data.lng", equalTo(returnLng)))

                        .andDo(document("place-detail-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("조회하려는 모임 장소의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 장소의 ID"),
                                        fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("모임 장소의 카카오 API ID"),
                                        fieldWithPath("category").type(JsonFieldType.STRING).description("모임 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("memo").type(JsonFieldType.STRING).description("모임 장소의 메모").optional(),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("모임 장소의 이름"),
                                        fieldWithPath("lat").type(JsonFieldType.NUMBER).description("모임 장소의 위도"),
                                        fieldWithPath("lng").type(JsonFieldType.NUMBER).description("모임 장소의 경도")
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("없는 모임 장소에 대해 요청을 보낼 경우 Not Found를 응답한다")
            public void 예외_장소식별자() throws Exception {

                Long nonexistentPlaceId = 20L;

                willThrow(new CustomException("해당 ID와 일치하는 모임 장소를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceQueryService).getDetail(eq(mockedExistentMeetingId), eq(nonexistentPlaceId));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, nonexistentPlaceId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-detail-error-place-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("조회하려는 모임 장소의 ID")
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
                Long returnApiId = 1000L;
                PlaceCategory returnCategory = PlaceCategory.CAFE;
                String returnMemo = "memo";
                String returnName = "place name";
                Double returnLat = 10.1;
                Double returnLng = 20.1;

                MeetingPlaceDetailResponse returnResponse = MeetingPlaceDetailResponse.builder()
                        .id(existentPlaceId)
                        .apiId(returnApiId)
                        .category(returnCategory.getKorName())
                        .memo(returnMemo)
                        .name(returnName)
                        .lat(returnLat)
                        .lng(returnLng)
                        .build();

                given(meetingPlaceQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentPlaceId)))
                        .willReturn(returnResponse);

                willThrow(new CustomException("해당 ID와 일치하는 리소스를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingPlaceQueryService).getDetail(eq(mockedNonexistentMeetingId), eq(existentPlaceId));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places/{placeId}", mockedNonexistentMeetingId, existentPlaceId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-detail-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("조회하려는 모임 장소의 ID")
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
                Long returnApiId = 1000L;
                PlaceCategory returnCategory = PlaceCategory.CAFE;
                String returnMemo = "memo";
                String returnName = "place name";
                Double returnLat = 10.1;
                Double returnLng = 20.1;

                MeetingPlaceDetailResponse returnResponse = MeetingPlaceDetailResponse.builder()
                        .id(existentPlaceId)
                        .apiId(returnApiId)
                        .category(returnCategory.getKorName())
                        .memo(returnMemo)
                        .name(returnName)
                        .lat(returnLat)
                        .lng(returnLng)
                        .build();

                given(meetingPlaceQueryService.getDetail(eq(mockedExistentMeetingId), eq(existentPlaceId)))
                        .willReturn(returnResponse);

                String unJoinedToken = createToken(10L);

                mockMvc.perform(get("/meetings/{meetingId}/places/{placeId}", mockedExistentMeetingId, existentPlaceId)
                                .header("Authorization", unJoinedToken)
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("place-detail-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID"),
                                        parameterWithName("placeId").description("조회하려는 모임 장소의 ID")
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
    @DisplayName("모임장소 조회 - 리스트")
    class 모임장소리스트조회{

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적으로 조회될 경우 OK와 장소 정보 리스트를 응답한다.")
            public void 정상_흐름() throws Exception {

                Long returnId1 = 10L;
                Long returnApiId1 = 1000L;
                PlaceCategory returnCategory1 = PlaceCategory.CAFE;
                String returnMemo1 = "memo1";
                String returnName1 = "place name1";
                Double returnLat1 = 10.1;
                Double returnLng1 = 20.1;
                Integer returnOrder1 = 1;

                MeetingPlaceListResponse returnPlaceResponse1 = MeetingPlaceListResponse.builder()
                        .id(returnId1)
                        .apiId(returnApiId1)
                        .category(returnCategory1.getKorName())
                        .name(returnName1)
                        .lat(returnLat1)
                        .lng(returnLng1)
                        .memo(returnMemo1)
                        .order(returnOrder1)
                        .build();

                Long returnId2 = 11L;
                Long returnApiId2 = 2000L;
                PlaceCategory returnCategory2 = PlaceCategory.ACCOMMODATION;
                String returnMemo2 = "memo2";
                String returnName2 = "place name2";
                Double returnLat2 = 110.1;
                Double returnLng2 = 90.1;
                Integer returnOrder2 = 2;

                MeetingPlaceListResponse returnPlaceResponse2 = MeetingPlaceListResponse.builder()
                        .id(returnId2)
                        .apiId(returnApiId2)
                        .category(returnCategory2.getKorName())
                        .name(returnName2)
                        .lat(returnLat2)
                        .lng(returnLng2)
                        .memo(returnMemo2)
                        .order(returnOrder2)
                        .build();

                Long returnId3 = 12L;
                Long returnApiId3 = 3000L;
                PlaceCategory returnCategory3 = PlaceCategory.ACCOMMODATION;
                String returnMemo3 = "memo3";
                String returnName3 = "place name3";
                Double returnLat3 = 50.1;
                Double returnLng3 = 20.1;
                Integer returnOrder3 = 3;

                MeetingPlaceListResponse returnPlaceResponse3 = MeetingPlaceListResponse.builder()
                        .id(returnId3)
                        .apiId(returnApiId3)
                        .category(returnCategory3.getKorName())
                        .name(returnName3)
                        .lat(returnLat3)
                        .lng(returnLng3)
                        .memo(returnMemo3)
                        .order(returnOrder3)
                        .build();

                List<MeetingPlaceListResponse> returnPlaceReponses = new ArrayList<>();
                returnPlaceReponses.add(returnPlaceResponse1);
                returnPlaceReponses.add(returnPlaceResponse2);
                returnPlaceReponses.add(returnPlaceResponse3);

                ListResponse<MeetingPlaceListResponse> returnResponse
                        = ListResponse.createListResponse(returnPlaceReponses);

                given(meetingPlaceQueryService.getList(eq(mockedExistentMeetingId)))
                        .willReturn(returnResponse);

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", hostUserToken)
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data.count", equalTo(returnPlaceReponses.size())))

                        .andExpect(jsonPath("$.data.contents[0].id", equalTo(returnId1), Long.class))
                        .andExpect(jsonPath("$.data.contents[0].apiId", equalTo(returnApiId1), Long.class))
                        .andExpect(jsonPath("$.data.contents[0].category", equalTo(returnCategory1.getKorName())))
                        .andExpect(jsonPath("$.data.contents[0].name", equalTo(returnName1)))
                        .andExpect(jsonPath("$.data.contents[0].lat", equalTo(returnLat1)))
                        .andExpect(jsonPath("$.data.contents[0].lng", equalTo(returnLng1)))
                        .andExpect(jsonPath("$.data.contents[0].memo", equalTo(returnMemo1)))
                        .andExpect(jsonPath("$.data.contents[0].order", equalTo(returnOrder1)))

                        .andExpect(jsonPath("$.data.contents[1].id", equalTo(returnId2), Long.class))
                        .andExpect(jsonPath("$.data.contents[1].apiId", equalTo(returnApiId2), Long.class))
                        .andExpect(jsonPath("$.data.contents[1].category", equalTo(returnCategory2.getKorName())))
                        .andExpect(jsonPath("$.data.contents[1].name", equalTo(returnName2)))
                        .andExpect(jsonPath("$.data.contents[1].lat", equalTo(returnLat2)))
                        .andExpect(jsonPath("$.data.contents[1].lng", equalTo(returnLng2)))
                        .andExpect(jsonPath("$.data.contents[1].memo", equalTo(returnMemo2)))
                        .andExpect(jsonPath("$.data.contents[1].order", equalTo(returnOrder2)))

                        .andExpect(jsonPath("$.data.contents[2].id", equalTo(returnId3), Long.class))
                        .andExpect(jsonPath("$.data.contents[2].apiId", equalTo(returnApiId3), Long.class))
                        .andExpect(jsonPath("$.data.contents[2].category", equalTo(returnCategory3.getKorName())))
                        .andExpect(jsonPath("$.data.contents[2].name", equalTo(returnName3)))
                        .andExpect(jsonPath("$.data.contents[2].lat", equalTo(returnLat3)))
                        .andExpect(jsonPath("$.data.contents[2].lng", equalTo(returnLng3)))
                        .andExpect(jsonPath("$.data.contents[2].memo", equalTo(returnMemo3)))
                        .andExpect(jsonPath("$.data.contents[2].order", equalTo(returnOrder3)))

                        .andDo(document("place-list-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("count").type(JsonFieldType.NUMBER).description("총 응답된 리스트의 요소 수"),
                                        subsectionWithPath("contents").type(JsonFieldType.ARRAY).description("모임 장소들")
                                ),
                                responseFields(beneathPath("data.contents.[]").withSubsectionId("contents"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 장소의 ID"),
                                        fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("모임 장소의 카카오 맵 API ID"),
                                        fieldWithPath("category").type(JsonFieldType.STRING).description("모임 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("모임 장소의 이름"),
                                        fieldWithPath("lat").type(JsonFieldType.NUMBER).description("모임 장소의 위도"),
                                        fieldWithPath("lng").type(JsonFieldType.NUMBER).description("모임 장소의 경도"),
                                        fieldWithPath("memo").type(JsonFieldType.STRING).description("모임 장소의 메모"),
                                        fieldWithPath("order").type(JsonFieldType.NUMBER).description("모임 장소의 순서")
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

                ListResponse emptyResponse = ListResponse.createListResponse(new ArrayList<>());

                given(meetingPlaceQueryService.getList(eq(mockedNonexistentMeetingId)))
                        .willReturn(emptyResponse);

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places", mockedNonexistentMeetingId)
                                .header("Authorization", hostUserToken)
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("place-list-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID")
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

                Long returnId1 = 10L;
                Long returnApiId1 = 1000L;
                PlaceCategory returnCategory1 = PlaceCategory.CAFE;
                String returnMemo1 = "memo1";
                String returnName1 = "place name1";
                Double returnLat1 = 10.1;
                Double returnLng1 = 20.1;
                Integer returnOrder1 = 1;

                MeetingPlaceListResponse returnPlaceResponse1 = MeetingPlaceListResponse.builder()
                        .id(returnId1)
                        .apiId(returnApiId1)
                        .category(returnCategory1.getKorName())
                        .name(returnName1)
                        .lat(returnLat1)
                        .lng(returnLng1)
                        .memo(returnMemo1)
                        .order(returnOrder1)
                        .build();

                Long returnId2 = 11L;
                Long returnApiId2 = 2000L;
                PlaceCategory returnCategory2 = PlaceCategory.ACCOMMODATION;
                String returnMemo2 = "memo2";
                String returnName2 = "place name2";
                Double returnLat2 = 110.1;
                Double returnLng2 = 90.1;
                Integer returnOrder2 = 2;

                MeetingPlaceListResponse returnPlaceResponse2 = MeetingPlaceListResponse.builder()
                        .id(returnId2)
                        .apiId(returnApiId2)
                        .category(returnCategory2.getKorName())
                        .name(returnName2)
                        .lat(returnLat2)
                        .lng(returnLng2)
                        .memo(returnMemo2)
                        .order(returnOrder2)
                        .build();

                Long returnId3 = 12L;
                Long returnApiId3 = 3000L;
                PlaceCategory returnCategory3 = PlaceCategory.ACCOMMODATION;
                String returnMemo3 = "memo3";
                String returnName3 = "place name3";
                Double returnLat3 = 50.1;
                Double returnLng3 = 20.1;
                Integer returnOrder3 = 3;

                MeetingPlaceListResponse returnPlaceResponse3 = MeetingPlaceListResponse.builder()
                        .id(returnId3)
                        .apiId(returnApiId3)
                        .category(returnCategory3.getKorName())
                        .name(returnName3)
                        .lat(returnLat3)
                        .lng(returnLng3)
                        .memo(returnMemo3)
                        .order(returnOrder3)
                        .build();

                List<MeetingPlaceListResponse> returnPlaceReponses = new ArrayList<>();
                returnPlaceReponses.add(returnPlaceResponse1);
                returnPlaceReponses.add(returnPlaceResponse2);
                returnPlaceReponses.add(returnPlaceResponse3);

                ListResponse<MeetingPlaceListResponse> returnResponse
                        = ListResponse.createListResponse(returnPlaceReponses);

                given(meetingPlaceQueryService.getList(eq(mockedExistentMeetingId)))
                        .willReturn(returnResponse);

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(get("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", unJoinedUserToken)
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("place-list-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST권한이 없는 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                Long returnId1 = 10L;
                Long returnApiId1 = 1000L;
                PlaceCategory returnCategory1 = PlaceCategory.CAFE;
                String returnMemo1 = "memo1";
                String returnName1 = "place name1";
                Double returnLat1 = 10.1;
                Double returnLng1 = 20.1;
                Integer returnOrder1 = 1;

                MeetingPlaceListResponse returnPlaceResponse1 = MeetingPlaceListResponse.builder()
                        .id(returnId1)
                        .apiId(returnApiId1)
                        .category(returnCategory1.getKorName())
                        .name(returnName1)
                        .lat(returnLat1)
                        .lng(returnLng1)
                        .memo(returnMemo1)
                        .order(returnOrder1)
                        .build();

                Long returnId2 = 11L;
                Long returnApiId2 = 2000L;
                PlaceCategory returnCategory2 = PlaceCategory.ACCOMMODATION;
                String returnMemo2 = "memo2";
                String returnName2 = "place name2";
                Double returnLat2 = 110.1;
                Double returnLng2 = 90.1;
                Integer returnOrder2 = 2;

                MeetingPlaceListResponse returnPlaceResponse2 = MeetingPlaceListResponse.builder()
                        .id(returnId2)
                        .apiId(returnApiId2)
                        .category(returnCategory2.getKorName())
                        .name(returnName2)
                        .lat(returnLat2)
                        .lng(returnLng2)
                        .memo(returnMemo2)
                        .order(returnOrder2)
                        .build();

                Long returnId3 = 12L;
                Long returnApiId3 = 3000L;
                PlaceCategory returnCategory3 = PlaceCategory.ACCOMMODATION;
                String returnMemo3 = "memo3";
                String returnName3 = "place name3";
                Double returnLat3 = 50.1;
                Double returnLng3 = 20.1;
                Integer returnOrder3 = 3;

                MeetingPlaceListResponse returnPlaceResponse3 = MeetingPlaceListResponse.builder()
                        .id(returnId3)
                        .apiId(returnApiId3)
                        .category(returnCategory3.getKorName())
                        .name(returnName3)
                        .lat(returnLat3)
                        .lng(returnLng3)
                        .memo(returnMemo3)
                        .order(returnOrder3)
                        .build();

                List<MeetingPlaceListResponse> returnPlaceReponses = new ArrayList<>();
                returnPlaceReponses.add(returnPlaceResponse1);
                returnPlaceReponses.add(returnPlaceResponse2);
                returnPlaceReponses.add(returnPlaceResponse3);

                ListResponse<MeetingPlaceListResponse> returnResponse
                        = ListResponse.createListResponse(returnPlaceReponses);

                given(meetingPlaceQueryService.getList(eq(mockedExistentMeetingId)))
                        .willReturn(returnResponse);

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}/places", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                        )
                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("place-list-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 모임에 가입된 회원인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회할 모임 장소가 포함된 모임의 ID")
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