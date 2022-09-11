package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.SliceResponse;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.query.MeetingCondition;
import com.comeon.meetingservice.web.meeting.response.*;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeetingControllerTest extends ControllerTestBase {

    MockMultipartFile sampleFile;

    @Value("${meeting-file.dir}")
    String sampleDir;

    UploadFileDto uploadFileDto;

    @BeforeEach
    public void init() {
        String sampleFileOriName = "test.png";

        sampleFile = new MockMultipartFile(
                "image",
                sampleFileOriName,
                ContentType.IMAGE_PNG.getMimeType(),
                "test data".getBytes(StandardCharsets.UTF_8));

        uploadFileDto = UploadFileDto.builder()
                .storedFileName("storedName")
                .originalFileName(sampleFileOriName)
                .build();

        given(fileManager.upload(refEq(sampleFile), eq(sampleDir)))
                .willReturn(uploadFileDto);
    }

    @Nested
    @DisplayName("모임 저장")
    class 모임저장 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("모든 필수 데이터가 넘어온 경우 OK를 응답한다.")
            public void 모임_저장_정상흐름() throws Exception {

                Long addedUserId = mockedHostUserId;
                LocalDate addedStartDate = LocalDate.of(2022, 06, 10);
                LocalDate addedEndDate = LocalDate.of(2022, 06, 30);
                String addedTitle = "title";

                MeetingAddDto normalDto = MeetingAddDto.builder()
                        .userId(addedUserId)
                        .startDate(addedStartDate)
                        .endDate(addedEndDate)
                        .title(addedTitle)
                        .originalFileName(uploadFileDto.getOriginalFileName())
                        .storedFileName(uploadFileDto.getStoredFileName())
                        .build();

                Long createdMeetingId = 10L;
                given(meetingService.add(refEq(normalDto))).willReturn(createdMeetingId);

                String meetingCreatorToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings")
                                .file(sampleFile)
                                .param("title", addedTitle)
                                .param("startDate", addedStartDate.toString())
                                .param("endDate", addedEndDate.toString())
                                .header("Authorization", meetingCreatorToken)
                        )

                        .andExpect(status().isCreated())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                        .andExpect(jsonPath("$.data", equalTo(createdMeetingId), Long.class))

                        .andDo(document("meeting-create-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                requestParameters(
                                        parameterWithName("title").description("모임 제목"),
                                        parameterWithName("startDate").description("시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("종료일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("courseId").description("장소를 참조할 코스의 ID").optional()
                                ),
                                requestParts(
                                        partWithName("image").description("모임 이미지")
                                ))
                        )
                ;
            }

        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("필수 데이터가 넘어오지 않은 경우 Bad Request를 응답한다.")
            public void 예외_필수값() throws Exception {

                Long addedUserId = mockedHostUserId;
                LocalDate addedStartDate = LocalDate.of(2022, 06, 10);
                LocalDate addedEndDate = LocalDate.of(2022, 06, 30);
                String addedTitle = "title";

                MeetingAddDto normalDto = MeetingAddDto.builder()
                        .userId(addedUserId)
                        .startDate(addedStartDate)
                        .endDate(addedEndDate)
                        .title(addedTitle)
                        .originalFileName(uploadFileDto.getOriginalFileName())
                        .storedFileName(uploadFileDto.getStoredFileName())
                        .build();

                Long createdMeetingId = 10L;
                given(meetingService.add(refEq(normalDto))).willReturn(createdMeetingId);

                String meetingCreatorToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings")
                                .file(sampleFile)
                                .header("Authorization", meetingCreatorToken)
                        )

                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))

                        .andDo(document("meeting-create-error-param",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                requestParts(
                                        partWithName("image").description("모임 이미지")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("어떤 파라미터가 검증에 실패했는지 표시"),
                                        fieldWithPath("message.title").type(JsonFieldType.ARRAY).description("검증에 실패한 이유"),
                                        fieldWithPath("message.endDate").type(JsonFieldType.ARRAY).description("검증에 실패한 이유"),
                                        fieldWithPath("message.startDate").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")

                                ))
                        )
                ;
            }

            @Test
            @DisplayName("저장에 실패한 경우 Internal Server Error를 응답한다.")
            public void 예외_저장오류() throws Exception {

                String errorFileOriName = "error.png";

                MockMultipartFile uploadErrorFile = new MockMultipartFile(
                        "image",
                        errorFileOriName,
                        ContentType.IMAGE_PNG.getMimeType(),
                        "Upload Error".getBytes(StandardCharsets.UTF_8));

                willThrow(new CustomException("서버 파일 저장 오류", ErrorCode.UPLOAD_FAIL))
                        .given(fileManager).upload(refEq(uploadErrorFile), eq(sampleDir));

                Long addedUserId = mockedHostUserId;
                LocalDate addedStartDate = LocalDate.of(2022, 06, 10);
                LocalDate addedEndDate = LocalDate.of(2022, 06, 30);
                String addedTitle = "title";

                MeetingAddDto normalDto = MeetingAddDto.builder()
                        .userId(addedUserId)
                        .startDate(addedStartDate)
                        .endDate(addedEndDate)
                        .title(addedTitle)
                        .originalFileName(uploadFileDto.getOriginalFileName())
                        .storedFileName(uploadFileDto.getStoredFileName())
                        .build();

                Long createdMeetingId = 10L;
                given(meetingService.add(refEq(normalDto))).willReturn(createdMeetingId);

                String meetingCreatorToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings")
                                .file(uploadErrorFile)
                                .param("title", addedTitle)
                                .param("startDate", addedStartDate.toString())
                                .param("endDate", addedEndDate.toString())
                                .header("Authorization", meetingCreatorToken)
                        )

                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SERVER_ERROR.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.UPLOAD_FAIL.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.UPLOAD_FAIL.getMessage())))

                        .andDo(document("meeting-create-error-upload",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                requestParameters(
                                        parameterWithName("title").description("모임 제목"),
                                        parameterWithName("startDate").description("시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("종료일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("courseId").description("장소를 참조할 코스의 ID").optional()
                                ),
                                requestParts(
                                        partWithName("image").description("모임 이미지")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("어떤 파라미터가 검증에 실패했는지 표시")
                                ))
                        )
                ;
            }
        }
    }


    @Nested
    @DisplayName("모임 수정")
    class 모임수정 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("이미지를 포함하여 수정할 경우 정상 수행 시 OK를 응답한다.")
            public void 정상흐름_이미지포함() throws Exception {

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto imageIncludedDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .originalFileName(uploadFileDto.getOriginalFileName())
                        .storedFileName(uploadFileDto.getStoredFileName())
                        .build();

                willDoNothing().given(meetingService).modify(refEq(imageIncludedDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedExistentMeetingId)
                                .file(sampleFile)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("meeting-modify-normal-include-image",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("title").description("수정할 모임 제목"),
                                        parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                                ),
                                requestParts(
                                        partWithName("image").description("수정할 모임 이미지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("이미지를 미포함하여 수정할 경우 정상 수행 시 OK를 응답한다.")
            public void 정상흐름_이미지미포함() throws Exception {

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto imageExcludedDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(imageExcludedDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedExistentMeetingId)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("meeting-modify-normal-exclude-image",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("title").description("수정할 모임 제목"),
                                        parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                                ))
                        )
                ;
            }
        }

        @Nested
        @DisplayName("예외")
        class 예외 {

            @Test
            @DisplayName("필수 데이터를 보내지 않은 경우 Bad Request를 응답한다.")
            public void 필수값_예외() throws Exception {

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto normalDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(normalDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedExistentMeetingId)
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", hostUserToken)
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.BAD_PARAMETER.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))

                        .andDo(document("meeting-modify-error-param",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.OBJECT).description("어떤 파라미터가 검증에 실패했는지 표시"),
                                        fieldWithPath("message.title").type(JsonFieldType.ARRAY).description("검증에 실패한 이유"),
                                        fieldWithPath("message.startDate").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("이미지 포함 수정 시 저장에 실패한 경우 Internal Server Error를 응답한다.")
            public void 예외_저장오류() throws Exception {

                String errorFileOriName = "error.png";

                MockMultipartFile uploadErrorFile = new MockMultipartFile(
                        "image",
                        errorFileOriName,
                        ContentType.IMAGE_PNG.getMimeType(),
                        "Upload Error".getBytes(StandardCharsets.UTF_8));

                willThrow(new CustomException("서버 파일 저장 오류", ErrorCode.UPLOAD_FAIL))
                        .given(fileManager).upload(refEq(uploadErrorFile), eq(sampleDir));

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto normalDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(normalDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings")
                                .file(uploadErrorFile)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isInternalServerError())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SERVER_ERROR.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.UPLOAD_FAIL.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.UPLOAD_FAIL.getMessage())))

                        .andDo(document("meeting-modify-error-upload",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                requestParameters(
                                        parameterWithName("title").description("모임 제목"),
                                        parameterWithName("startDate").description("시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("종료일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("courseId").description("장소를 참조할 코스의 ID").optional()
                                ),
                                requestParts(
                                        partWithName("image").description("모임 이미지")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("어떤 파라미터가 검증에 실패했는지 표시")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("없는 모임에 대해 요청을 보낼 경우 Not Found를 응답한다.")
            public void 예외_모임식별자() throws Exception {

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto normalDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(normalDto));

                String hostUserToken = createToken(mockedHostUserId);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedNonexistentMeetingId)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", hostUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("meeting-modify-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("title").description("수정할 모임 제목"),
                                        parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
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

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto normalDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(normalDto));

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedExistentMeetingId)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", unJoinedUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("meeting-modify-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("title").description("수정할 모임 제목"),
                                        parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                                ))
                        )
                ;
            }

            @Test
            @DisplayName("HOST가 아닌 회원이 요청을 보낼 경우 Forbidden을 응답한다.")
            public void 예외_회원권한() throws Exception {

                LocalDate modifiedStartDate = LocalDate.of(2022, 07, 10);
                LocalDate modifiedEndDate = LocalDate.of(2022, 07, 30);
                String modifiedTitle = "title";

                MeetingModifyDto normalDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(normalDto));

                String editorUserToken = createToken(mockedEditorUserId);

                mockMvc.perform(multipart("/meetings/{meetingId}", mockedExistentMeetingId)
                                .param("title", modifiedTitle)
                                .param("startDate", modifiedStartDate.toString())
                                .param("endDate", modifiedEndDate.toString())
                                .header("Authorization", editorUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.AUTHORIZATION_FAIL.getCode())))

                        .andDo(document("meeting-modify-error-authorization",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 HOST인 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("수정하려는 모임의 ID")
                                ),
                                requestParameters(
                                        parameterWithName("title").description("수정할 모임 제목"),
                                        parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
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
    @DisplayName("모임 삭제")
    class 모임삭제 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적으로 삭제될 경우 OK를 응답한다.")
            public void 정상_흐름() throws Exception {

                MeetingRemoveDto normalDto = MeetingRemoveDto.builder()
                        .id(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .build();

                willDoNothing().given(meetingService).remove(refEq(normalDto));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(delete("/meetings/{meetingId}", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        .andDo(document("meeting-delete-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제(탈퇴)하려는 모임의 ID")
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

                MeetingRemoveDto normalDto = MeetingRemoveDto.builder()
                        .id(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .build();

                willDoNothing().given(meetingService).remove(refEq(normalDto));

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(delete("/meetings/{meetingId}", mockedNonexistentMeetingId)
                                .header("Authorization", participantUserToken))

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("meeting-delete-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제(탈퇴)하려는 모임의 ID")
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
                // given
                MeetingRemoveDto normalDto = MeetingRemoveDto.builder()
                        .id(mockedExistentMeetingId)
                        .userId(mockedParticipantUserId)
                        .build();

                willDoNothing().given(meetingService).remove(refEq(normalDto));

                String unJoinedUserToken = createToken(10L);

                mockMvc.perform(delete("/meetings/{meetingId}", mockedExistentMeetingId)
                                .header("Authorization", unJoinedUserToken))

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("meeting-delete-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("삭제(탈퇴)하려는 모임의 ID")
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
    @DisplayName("모임조회-리스트")
    class 모임리스트조회 {

        @Test
        @DisplayName("정상적으로 조회될 경우 OK와 모임 정보를 리스트로 응답한다.")
        public void 정상_흐름() throws Exception {

            String sampleTitleCond = "title";
            LocalDate sampleStartDateCond = LocalDate.of(2022, 06, 10);
            LocalDate sampleEndDateCond = LocalDate.of(2022, 07, 30);
            int samplePage = 0;
            int sampleSize = 5;
            Pageable samplePageable = PageRequest.of(samplePage, sampleSize);

            MeetingCondition sampleCondition = MeetingCondition.builder()
                    .title(sampleTitleCond)
                    .startDate(sampleStartDateCond)
                    .endDate(sampleEndDateCond)
                    .build();

            Long responseMeetingId1 = 10L;
            String responseHostNickname1 = "host nickname1";
            Integer responseUserCount1 = 3;
            MeetingRole responseMeetingRole1 = MeetingRole.HOST;
            String responseTitle1 = "title1";
            LocalDate responseStartDate1 = LocalDate.of(2022, 06, 15);
            LocalDate responseEndDate1 = LocalDate.of(2022, 06, 20);
            Long responseMeetingCodeId1 = 100L;
            String responseImageLink1 = "https://link1";
            List<LocalDate> responseFixedDates1 = new ArrayList<>();
            responseFixedDates1.add(LocalDate.of(2022, 06, 16));
            responseFixedDates1.add(LocalDate.of(2022, 06, 17));
            MeetingStatus responseMeetingStatus1 = MeetingStatus.END;

            MeetingListResponse meetingListResponse1 = MeetingListResponse.builder()
                    .id(responseMeetingId1)
                    .hostNickname(responseHostNickname1)
                    .userCount(responseUserCount1)
                    .myMeetingRole(responseMeetingRole1)
                    .title(responseTitle1)
                    .startDate(responseStartDate1)
                    .endDate(responseEndDate1)
                    .meetingCodeId(responseMeetingCodeId1)
                    .imageLink(responseImageLink1)
                    .fixedDates(responseFixedDates1)
                    .meetingStatus(responseMeetingStatus1)
                    .build();

            Long responseMeetingId2 = 20L;
            String responseHostNickname2 = "host nickname2";
            Integer responseUserCount2 = 5;
            MeetingRole responseMeetingRole2 = MeetingRole.EDITOR;
            String responseTitle2 = "title2";
            LocalDate responseStartDate2 = LocalDate.of(2022, 07, 10);
            LocalDate responseEndDate2 = LocalDate.of(2022, 07, 25);
            Long responseMeetingCodeId2 = 200L;
            String responseImageLink2 = "https://link1";
            List<LocalDate> responseFixedDates2 = new ArrayList<>();
            MeetingStatus responseMeetingStatus2 = MeetingStatus.UNFIXED;

            MeetingListResponse meetingListResponse2 = MeetingListResponse.builder()
                    .id(responseMeetingId2)
                    .hostNickname(responseHostNickname2)
                    .userCount(responseUserCount2)
                    .myMeetingRole(responseMeetingRole2)
                    .title(responseTitle2)
                    .startDate(responseStartDate2)
                    .endDate(responseEndDate2)
                    .meetingCodeId(responseMeetingCodeId2)
                    .imageLink(responseImageLink2)
                    .fixedDates(responseFixedDates2)
                    .meetingStatus(responseMeetingStatus2)
                    .build();

            List<MeetingListResponse> responseContents = new ArrayList<>();
            responseContents.add(meetingListResponse1);
            responseContents.add(meetingListResponse2);

            boolean hasNext = false;
            SliceImpl sampleResultSlice = new SliceImpl(responseContents, samplePageable, hasNext);
            SliceResponse sampleResultResponse = SliceResponse.toSliceResponse(sampleResultSlice);

            given(meetingQueryService.getList(
                    eq(mockedHostUserId),
                    refEq(samplePageable),
                    refEq(sampleCondition))).willReturn(sampleResultResponse);

            String hostUserToken = createToken(mockedHostUserId);

            mockMvc.perform(get("/meetings")
                            .header("Authorization", hostUserToken)
                            .queryParam("page", String.valueOf(samplePage))
                            .queryParam("size", String.valueOf(sampleSize))
                            .queryParam("title", sampleTitleCond)
                            .queryParam("startDate", sampleStartDateCond.toString())
                            .queryParam("endDate", sampleEndDateCond.toString())
                    )

                    // SliceInfo
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))
                    .andExpect(jsonPath("$.data.currentSlice", equalTo(samplePage)))
                    .andExpect(jsonPath("$.data.sizePerSlice", equalTo(sampleSize)))
                    .andExpect(jsonPath("$.data.numberOfElements", equalTo(responseContents.size())))
                    .andExpect(jsonPath("$.data.hasPrevious", equalTo(sampleResultSlice.hasPrevious())))
                    .andExpect(jsonPath("$.data.hasNext", equalTo(sampleResultSlice.hasNext())))
                    .andExpect(jsonPath("$.data.first", equalTo(sampleResultResponse.isFirst())))
                    .andExpect(jsonPath("$.data.last", equalTo(sampleResultResponse.isLast())))

                    // MeetingData1
                    .andExpect(jsonPath("$.data.contents[0].id", equalTo(responseMeetingId1), Long.class))
                    .andExpect(jsonPath("$.data.contents[0].hostNickname", equalTo(responseHostNickname1)))
                    .andExpect(jsonPath("$.data.contents[0].userCount", equalTo(responseUserCount1)))
                    .andExpect(jsonPath("$.data.contents[0].myMeetingRole", equalTo(responseMeetingRole1.name())))
                    .andExpect(jsonPath("$.data.contents[0].title", containsString(sampleTitleCond)))
                    .andExpect(jsonPath("$.data.contents[0].startDate", greaterThanOrEqualTo(sampleStartDateCond.toString())))
                    .andExpect(jsonPath("$.data.contents[0].endDate", lessThanOrEqualTo(sampleEndDateCond.toString())))
                    .andExpect(jsonPath("$.data.contents[0].meetingCodeId", equalTo(responseMeetingCodeId1), Long.class))
                    .andExpect(jsonPath("$.data.contents[0].imageLink", equalTo(responseImageLink1)))
                    .andExpect(jsonPath("$.data.contents[0].fixedDates[0]", equalTo(meetingListResponse1.getFixedDates().get(0).toString())))
                    .andExpect(jsonPath("$.data.contents[0].fixedDates[1]", equalTo(meetingListResponse1.getFixedDates().get(1).toString())))
                    .andExpect(jsonPath("$.data.contents[0].meetingStatus", equalTo(responseMeetingStatus1.name())))

                    // MeetingData2
                    .andExpect(jsonPath("$.data.contents[1].id", equalTo(responseMeetingId2), Long.class))
                    .andExpect(jsonPath("$.data.contents[1].hostNickname", equalTo(responseHostNickname2)))
                    .andExpect(jsonPath("$.data.contents[1].userCount", equalTo(responseUserCount2)))
                    .andExpect(jsonPath("$.data.contents[1].myMeetingRole", equalTo(responseMeetingRole2.name())))
                    .andExpect(jsonPath("$.data.contents[1].title", containsString(sampleTitleCond)))
                    .andExpect(jsonPath("$.data.contents[1].startDate", greaterThanOrEqualTo(sampleStartDateCond.toString())))
                    .andExpect(jsonPath("$.data.contents[1].endDate", lessThanOrEqualTo(sampleEndDateCond.toString())))
                    .andExpect(jsonPath("$.data.contents[1].meetingCodeId", equalTo(responseMeetingCodeId2), Long.class))
                    .andExpect(jsonPath("$.data.contents[1].imageLink", equalTo(responseImageLink2)))
                    .andExpect(jsonPath("$.data.contents[1].fixedDates", empty()))
                    .andExpect(jsonPath("$.data.contents[1].meetingStatus", equalTo(responseMeetingStatus2.name())))

                    .andDo(document("meeting-list-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    headerWithName("Authorization").description("회원의 Bearer 토큰").attributes(key("format").value("Bearer somejwttokens..."))
                            ),
                            requestParameters(
                                    parameterWithName("page").description("조회할 페이지(슬라이스), 기본값: 0").optional(),
                                    parameterWithName("size").description("한 페이지(슬라이스)당 조회할 데이터 수, 기본값: 5").optional(),
                                    parameterWithName("title").description("검색할 모임 제목, 해당 제목이 포함되어 있는 데이터를 조회").optional(),
                                    parameterWithName("startDate").description("검색할 시작일, 모임 시작일이 해당 날짜와 같거나 이후 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional(),
                                    parameterWithName("endDate").description("검색할 종료일, 모임 종료일이 해당 날짜와 같거나 이전 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional()
                            ),
                            responseFields(beneathPath("data.contents").withSubsectionId("contents"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임의 ID"),
                                    fieldWithPath("hostNickname").type(JsonFieldType.STRING).description("해당 모임의 HOST 유저 닉네임").optional(),
                                    fieldWithPath("userCount").type(JsonFieldType.NUMBER).description("해당 모임의 총 회원 수").optional(),
                                    fieldWithPath("myMeetingRole").type(JsonFieldType.STRING).description("해당 모임에서 요청을 보낸 회원의 역할").attributes(key("format").value("HOST, EDITOR, PARTICIPANT")),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("모임의 제목"),
                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("모임의 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("모임의 종료일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("모임의 이미지 링크"),
                                    fieldWithPath("meetingCodeId").type(JsonFieldType.NUMBER).description("모임의 초대코드 아이디"),
                                    fieldWithPath("fixedDates").type(JsonFieldType.ARRAY).description("모임의 확정 날짜들"),
                                    fieldWithPath("meetingStatus").type(JsonFieldType.STRING).description("모임의 상태, 확정된 날짜가 없음/진행중/끝").attributes(key("format").value("UNFIXED, PROCEEDING, END"))
                            ))
                    )
            ;
        }
    }

    @Nested
    @DisplayName("모임조회-단건")
    class 모임단건조회 {

        @Nested
        @DisplayName("정상흐름")
        class 정상흐름 {

            @Test
            @DisplayName("정상적으로 조회될 경우 OK와 모임 정보를 응답한다.")
            public void 정상_흐름() throws Exception {

                // === 모임 정보 === //
                Long responseMeetingUserId = 11L;
                MeetingRole responseMeetingRole = MeetingRole.PARTICIPANT;
                String responseTitle = "title";
                LocalDate responseStartDate = LocalDate.of(2022, 06, 10);
                LocalDate responseEndDate = LocalDate.of(2022, 06, 30);

                // === 모임 유저들 === //
                List<MeetingDetailUserResponse> responseMeetingUsers = new ArrayList<>();

                Long responseUserId1 = 10L;
                String responseUserNickname1 = "nickname1";
                String responseUserImageLink1 = "http://link1";
                MeetingRole responseUserMeetingRole1 = MeetingRole.HOST;

                MeetingDetailUserResponse responseMeetingUser1 = MeetingDetailUserResponse.builder()
                        .id(responseUserId1)
                        .nickname(responseUserNickname1)
                        .imageLink(responseUserImageLink1)
                        .meetingRole(responseUserMeetingRole1)
                        .build();

                responseMeetingUsers.add(responseMeetingUser1);

                Long responseUserId2 = 10L;
                String responseUserNickname2 = "nickname1";
                String responseUserImageLink2 = "http://link1";
                MeetingRole responseUserMeetingRole2 = MeetingRole.HOST;

                MeetingDetailUserResponse responseMeetingUser2 = MeetingDetailUserResponse.builder()
                        .id(responseUserId2)
                        .nickname(responseUserNickname2)
                        .imageLink(responseUserImageLink2)
                        .meetingRole(responseUserMeetingRole2)
                        .build();

                responseMeetingUsers.add(responseMeetingUser2);

                // === 모임 날짜들 === //
                List<MeetingDetailDateResponse> responseMeetingDates = new ArrayList<>();

                Long responseDateId1 = 10L;
                LocalDate responseDateDate1 = LocalDate.of(2022, 06, 15);
                Integer responseDateUserCount1 = 1;
                DateStatus responseDateDateStatus1 = DateStatus.UNFIXED;

                MeetingDetailDateResponse responseMeetingDate1 = MeetingDetailDateResponse.builder()
                        .id(responseDateId1)
                        .date(responseDateDate1)
                        .userCount(responseDateUserCount1)
                        .dateStatus(responseDateDateStatus1)
                        .build();

                responseMeetingDates.add(responseMeetingDate1);

                Long responseDateId2 = 11L;
                LocalDate responseDateDate2 = LocalDate.of(2022, 06, 25);
                Integer responseDateUserCount2 = 2;
                DateStatus responseDateDateStatus2 = DateStatus.FIXED;

                MeetingDetailDateResponse responseMeetingDate2 = MeetingDetailDateResponse.builder()
                        .id(responseDateId2)
                        .date(responseDateDate2)
                        .userCount(responseDateUserCount2)
                        .dateStatus(responseDateDateStatus2)
                        .build();

                responseMeetingDates.add(responseMeetingDate2);

                // === 모임 장소들 === //
                List<MeetingDetailPlaceResponse> responseMeetingPlaces = new ArrayList<>();

                Long responsePlaceId1 = 10L;
                Long responsePlaceApiId1 = 1000L;
                PlaceCategory responsePlaceCategory1 = PlaceCategory.BAR;
                String responsePlaceName1 = "place1";
                String responsePlaceMemo1 = "memo1";
                Double responsePlaceLat1 = 10.1;
                Double responsePlaceLng1 = 20.1;
                Integer responsePlaceOrder1 = 1;

                MeetingDetailPlaceResponse responseMeetingPlace1 = MeetingDetailPlaceResponse.builder()
                        .id(responsePlaceId1)
                        .apiId(responsePlaceApiId1)
                        .category(responsePlaceCategory1.getKorName())
                        .name(responsePlaceName1)
                        .memo(responsePlaceMemo1)
                        .lat(responsePlaceLat1)
                        .lng(responsePlaceLng1)
                        .order(responsePlaceOrder1)
                        .build();

                responseMeetingPlaces.add(responseMeetingPlace1);

                Long responsePlaceId2 = 11L;
                Long responsePlaceApiId2 = 2000L;
                PlaceCategory responsePlaceCategory2 = PlaceCategory.CAFE;
                String responsePlaceName2 = "place2";
                String responsePlaceMemo2 = "memo2";
                Double responsePlaceLat2 = 110.1;
                Double responsePlaceLng2 = 120.1;
                Integer responsePlaceOrder2 = 2;

                MeetingDetailPlaceResponse responseMeetingPlace2 = MeetingDetailPlaceResponse.builder()
                        .id(responsePlaceId2)
                        .apiId(responsePlaceApiId2)
                        .category(responsePlaceCategory2.getKorName())
                        .name(responsePlaceName2)
                        .memo(responsePlaceMemo2)
                        .lat(responsePlaceLat2)
                        .lng(responsePlaceLng2)
                        .order(responsePlaceOrder2)
                        .build();

                responseMeetingPlaces.add(responseMeetingPlace2);

                // === 최종 응답 데이터 작성 및 모킹 === //
                MeetingDetailResponse resultResponse = MeetingDetailResponse.builder()
                        .id(mockedExistentMeetingId)
                        .myMeetingUserId(responseMeetingUserId)
                        .myMeetingRole(responseMeetingRole)
                        .title(responseTitle)
                        .startDate(responseStartDate)
                        .endDate(responseEndDate)
                        .meetingUsers(responseMeetingUsers)
                        .meetingDates(responseMeetingDates)
                        .meetingPlaces(responseMeetingPlaces)
                        .build();

                given(meetingQueryService.getDetail(mockedExistentMeetingId, mockedParticipantUserId))
                        .willReturn(resultResponse);

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}", mockedExistentMeetingId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.SUCCESS.name())))

                        // Meeting
                        .andExpect(jsonPath("$.data.id", equalTo(mockedExistentMeetingId), Long.class))
                        .andExpect(jsonPath("$.data.myMeetingUserId", equalTo(responseMeetingUserId), Long.class))
                        .andExpect(jsonPath("$.data.myMeetingRole", equalTo(responseMeetingRole.name())))
                        .andExpect(jsonPath("$.data.title", equalTo(responseTitle)))
                        .andExpect(jsonPath("$.data.startDate", equalTo(responseStartDate.toString())))
                        .andExpect(jsonPath("$.data.endDate", equalTo(responseEndDate.toString())))

                        // MeetingUsers
                        .andExpect(jsonPath("$.data.meetingUsers[0].id", equalTo(responseUserId1), Long.class))
                        .andExpect(jsonPath("$.data.meetingUsers[0].nickname", equalTo(responseUserNickname1)))
                        .andExpect(jsonPath("$.data.meetingUsers[0].imageLink", equalTo(responseUserImageLink1)))
                        .andExpect(jsonPath("$.data.meetingUsers[0].meetingRole", equalTo(responseUserMeetingRole1.name())))
                        .andExpect(jsonPath("$.data.meetingUsers[1].id", equalTo(responseUserId2), Long.class))
                        .andExpect(jsonPath("$.data.meetingUsers[1].nickname", equalTo(responseUserNickname2)))
                        .andExpect(jsonPath("$.data.meetingUsers[1].imageLink", equalTo(responseUserImageLink2)))
                        .andExpect(jsonPath("$.data.meetingUsers[1].meetingRole", equalTo(responseUserMeetingRole2.name())))

                        // MeetingDates
                        .andExpect(jsonPath("$.data.meetingDates[0].id", equalTo(responseDateId1), Long.class))
                        .andExpect(jsonPath("$.data.meetingDates[0].date", equalTo(responseDateDate1.toString())))
                        .andExpect(jsonPath("$.data.meetingDates[0].userCount", equalTo(responseDateUserCount1)))
                        .andExpect(jsonPath("$.data.meetingDates[0].dateStatus", equalTo(responseDateDateStatus1.name())))
                        .andExpect(jsonPath("$.data.meetingDates[1].id", equalTo(responseDateId2), Long.class))
                        .andExpect(jsonPath("$.data.meetingDates[1].date", equalTo(responseDateDate2.toString())))
                        .andExpect(jsonPath("$.data.meetingDates[1].userCount", equalTo(responseDateUserCount2)))
                        .andExpect(jsonPath("$.data.meetingDates[1].dateStatus", equalTo(responseDateDateStatus2.name())))

                        // MeetingPlaces
                        .andExpect(jsonPath("$.data.meetingPlaces[0].id", equalTo(responsePlaceId1), Long.class))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].apiId", equalTo(responsePlaceApiId1), Long.class))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].category", equalTo(responsePlaceCategory1.getKorName())))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].name", equalTo(responsePlaceName1)))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].memo", equalTo(responsePlaceMemo1)))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].lat", equalTo(responsePlaceLat1)))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].lng", equalTo(responsePlaceLng1)))
                        .andExpect(jsonPath("$.data.meetingPlaces[0].order", equalTo(responsePlaceOrder1)))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].id", equalTo(responsePlaceId2), Long.class))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].apiId", equalTo(responsePlaceApiId2), Long.class))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].category", equalTo(responsePlaceCategory2.getKorName())))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].name", equalTo(responsePlaceName2)))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].memo", equalTo(responsePlaceMemo2)))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].lat", equalTo(responsePlaceLat2)))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].lng", equalTo(responsePlaceLng2)))
                        .andExpect(jsonPath("$.data.meetingPlaces[1].order", equalTo(responsePlaceOrder2)))

                        .andDo(document("meeting-detail-normal",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임의 ID")
                                ),
                                responseFields(beneathPath("data").withSubsectionId("data"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임의 ID"),
                                        fieldWithPath("myMeetingUserId").type(JsonFieldType.NUMBER).description("해당 모임에서 요청을 보낸 회원의 ID"),
                                        fieldWithPath("myMeetingRole").type(JsonFieldType.STRING).description("해당 모임에서 요청을 보낸 회원의 역할").attributes(key("format").value("HOST, EDITOR, PARTICIPANT")),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("모임의 제목"),
                                        fieldWithPath("startDate").type(JsonFieldType.STRING).description("모임의 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                        fieldWithPath("endDate").type(JsonFieldType.STRING).description("모임의 종료일").attributes(key("format").value("yyyy-MM-dd")),
                                        subsectionWithPath("meetingUsers").type(JsonFieldType.ARRAY).description("모임에 소속된 회원들"),
                                        subsectionWithPath("meetingDates").type(JsonFieldType.ARRAY).description("모임에서 선택된 날짜들"),
                                        subsectionWithPath("meetingPlaces").type(JsonFieldType.ARRAY).description("모임의 방문 장소들")
                                ),
                                responseFields(beneathPath("data.meetingUsers.[]").withSubsectionId("meeting-users"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 회원의 ID"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("모임 회원의 닉네임").optional(),
                                        fieldWithPath("imageLink").type(JsonFieldType.STRING).description("모임 회원의 프로필 이미지 링크").optional(),
                                        fieldWithPath("meetingRole").type(JsonFieldType.STRING).description("모임 회원의 역할").attributes(key("format").value("HOST, EDITOR, PARTICIPANT"))
                                ),
                                responseFields(beneathPath("data.meetingDates.[]").withSubsectionId("meeting-dates"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 날짜의 ID"),
                                        fieldWithPath("date").type(JsonFieldType.STRING).description("모임 날짜의 날짜").attributes(key("format").value("yyyy-MM-dd")),
                                        fieldWithPath("userCount").type(JsonFieldType.NUMBER).description("해당 모임 날짜를 선택한 유저 수"),
                                        fieldWithPath("dateStatus").type(JsonFieldType.STRING).description("해당 모임 날짜의 확정 여부").attributes(key("format").value("FIXED, UNFIXED"))
                                ),
                                responseFields(beneathPath("data.meetingPlaces.[]").withSubsectionId("meeting-places"),
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 장소의 ID"),
                                        fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("모임 장소의 카카오 맵 API ID"),
                                        fieldWithPath("category").type(JsonFieldType.STRING).description("모임 장소의 카테고리").attributes(key("format").value(categoryLink)),
                                        fieldWithPath("name").type(JsonFieldType.STRING).description("모임 장소의 이름"),
                                        fieldWithPath("memo").type(JsonFieldType.STRING).description("모임 장소의 메모"),
                                        fieldWithPath("lat").type(JsonFieldType.NUMBER).description("모임 장소의 위도"),
                                        fieldWithPath("lng").type(JsonFieldType.NUMBER).description("모임 장소의 경도"),
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
                willThrow(new CustomException("해당 ID와 일치하는 모임이 없음", ErrorCode.ENTITY_NOT_FOUND))
                        .given(meetingQueryService).getDetail(mockedNonexistentMeetingId, mockedParticipantUserId);

                String participantUserToken = createToken(mockedParticipantUserId);

                mockMvc.perform(get("/meetings/{meetingId}", mockedNonexistentMeetingId)
                                .header("Authorization", participantUserToken)
                        )

                        .andExpect(status().isNotFound())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.NOT_FOUND.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))

                        .andDo(document("meeting-detail-error-meeting-id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임의 ID")
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

                Long unJoinedUserId = 20L;
                willThrow(new CustomException("해당 ID와 일치하는 모임이 없음", ErrorCode.MEETING_USER_NOT_INCLUDE))
                        .given(meetingQueryService).getDetail(mockedExistentMeetingId, unJoinedUserId);

                String unJoinedUserToken = createToken(unJoinedUserId);

                mockMvc.perform(get("/meetings/{meetingId}", mockedExistentMeetingId)
                                .header("Authorization", unJoinedUserToken)
                        )

                        .andExpect(status().isForbidden())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.code", equalTo(ApiResponseCode.FORBIDDEN.name())))
                        .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                        .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))

                        .andDo(document("meeting-detail-error-not-joined",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestHeaders(
                                        headerWithName("Authorization").description("회원의 Bearer 토큰, 회원이 모임에 가입된 경우만 가능").attributes(key("format").value("Bearer somejwttokens..."))
                                ),
                                pathParameters(
                                        parameterWithName("meetingId").description("조회하려는 모임의 ID")
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