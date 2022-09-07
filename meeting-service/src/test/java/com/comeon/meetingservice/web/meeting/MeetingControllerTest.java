package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.query.MeetingQueryService;
import org.apache.http.entity.ContentType;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

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
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest extends ControllerTestBase {


    @MockBean
    MeetingService meetingService;

    @MockBean
    MeetingQueryService meetingQueryService;

    @MockBean
    FileManager fileManager;

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

                        .andDo(document("meeting-create-param",
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

                        .andDo(document("meeting-create-param",
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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .originalFileName(uploadFileDto.getOriginalFileName())
                        .storedFileName(uploadFileDto.getStoredFileName())
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                        .andDo(document("meeting-create-param",
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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder()
                        .id(mockedExistentMeetingId)
                        .startDate(modifiedStartDate)
                        .endDate(modifiedEndDate)
                        .title(modifiedTitle)
                        .build();

                willDoNothing().given(meetingService).modify(refEq(meetingModifyDto));

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

//
//    @Nested
//    @DisplayName("모임 삭제")
//    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//    class 모임삭제 {
//
//        @Test
//        @DisplayName("정상적으로 삭제될 경우")
//        public void 정상_흐름() throws Exception {
//            // given
//
//            mockMvc.perform(delete("/meetings/{meetingId}", 10)
//                            .header("Authorization", sampleToken))
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andDo(document("meeting-delete-normal",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint())
//                            )
//                    )
//            ;
//        }
//
//        @Test
//        @DisplayName("없는 모임 리소스를 탈퇴하려고 할 경우 NotFound와 예외 정보를 응답한다.")
//        public void 경로변수_예외() throws Exception {
//            // given
//
//            mockMvc.perform(delete("/meetings/{meetingId}", 5)
//                            .header("Authorization", sampleToken))
//                    .andExpect(status().isNotFound())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
//                    .andDo(document("meeting-delete-error-pathvariable",
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
//        @DisplayName("모임에 속해있지 않는 회원이 탈퇴요청을 보낼 경우")
//        public void 회원식별자_예외() throws Exception {
//            // given
//
//            String invalidToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEwLCJuYW1lIjo" +
//                    "iSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.Dd5xvXJhuMekBRlWM8fmdLZjqCUEj_K1dpIvZMaj8-w";
//
//            mockMvc.perform(delete("/meetings/{meetingId}", 10)
//                            .header("Authorization", invalidToken))
//                    .andExpect(status().isForbidden())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))
//                    .andDo(document("meeting-delete-error-userid",
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
//    @DisplayName("모임조회-리스트")
//    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//    class 모임리스트조회 {
//
//        @Test
//        @DisplayName("정상적으로 조회될 경우")
//        public void 정상_흐름() throws Exception {
//            // given
//
//            mockMvc.perform(get("/meetings")
//                            .header("Authorization", sampleToken)
//                            .queryParam("page", "0")
//                            .queryParam("size", "5")
//                            .queryParam("title", "tle")
//                            .queryParam("startDate", "2022-07-10")
//                            .queryParam("endDate", "2022-08-10")
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.currentSlice").exists())
//                    .andExpect(jsonPath("$.data.sizePerSlice").exists())
//                    .andExpect(jsonPath("$.data.numberOfElements").exists())
//                    .andExpect(jsonPath("$.data.hasPrevious").exists())
//                    .andExpect(jsonPath("$.data.hasNext").exists())
//                    .andExpect(jsonPath("$.data.first").exists())
//                    .andExpect(jsonPath("$.data.last").exists())
//
//                    .andDo(document("meeting-list-normal",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            requestParameters(
//                                    parameterWithName("page").description("조회할 페이지(슬라이스), 기본값: 0").optional(),
//                                    parameterWithName("size").description("한 페이지(슬라이스)당 조회할 데이터 수, 기본값: 5").optional(),
//                                    parameterWithName("title").description("검색할 모임 제목, 해당 제목이 포함되어 있는 데이터를 조회").optional(),
//                                    parameterWithName("startDate").description("검색할 시작일, 모임 시작일이 해당 날짜와 같거나 이후 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional(),
//                                    parameterWithName("endDate").description("검색할 종료일, 모임 종료일이 해당 날짜와 같거나 이전 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional()
//                            ),
//                            responseFields(beneathPath("data.contents").withSubsectionId("contents"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임의 ID"),
//                                    fieldWithPath("myMeetingRole").type(JsonFieldType.STRING).description("해당 모임에서 요청을 보낸 회원의 역할"),
//                                    fieldWithPath("title").type(JsonFieldType.STRING).description("모임의 제목"),
//                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("모임의 시작일").attributes(key("format").value("yyyy-MM-dd")),
//                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("모임의 종료일").attributes(key("format").value("yyyy-MM-dd")),
//                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("모임의 이미지 링크"),
//                                    fieldWithPath("meetingCodeId").type(JsonFieldType.NUMBER).description("모임의 초대코드 아이디"),
//                                    fieldWithPath("fixedDates").type(JsonFieldType.ARRAY).description("모임의 확정 날짜들"),
//                                    fieldWithPath("meetingStatus").type(JsonFieldType.STRING).description("모임의 상태, 확정된 날짜가 없음/진행중/끝").attributes(key("format").value("UNFIXED, PROCEEDING, END"))
//                            ))
//                    )
//            ;
//        }
//
//        @Test
//        @DisplayName("시작일 조건을 줄 경우 조건과 같거나 조건 이후의 시작일을 가진 데이터만 검색된다.")
//        public void 조건_시작일() throws Exception {
//            // given
//            String startDateCond = "2022-07-25";
//
//            mockMvc.perform(get("/meetings")
//                            .header("Authorization", sampleToken)
//                            .queryParam("startDate", startDateCond)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.contents[0].startDate", greaterThanOrEqualTo(startDateCond)))
//            ;
//        }
//
//        @Test
//        @DisplayName("종료일 조건을 줄 경우 조건과 같거나 조건 이전의 종료일을 가진 데이터만 검색된다.")
//        public void 조건_종료일() throws Exception {
//            // given
//            String endDateCond = "2022-07-25";
//
//            mockMvc.perform(get("/meetings")
//                            .header("Authorization", sampleToken)
//                            .queryParam("endDate", endDateCond)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.contents[0].endDate", lessThanOrEqualTo(endDateCond)))
//            ;
//        }
//
//        @Test
//        @DisplayName("제목 조건을 줄 경우 해당 조건이 제목에 포함된 데이터만 검색된다")
//        public void 조건_제목() throws Exception {
//            // given
//            String titleCond = "2";
//
//            mockMvc.perform(get("/meetings")
//                            .header("Authorization", sampleToken)
//                            .queryParam("title", titleCond)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.contents[0].title", containsString(titleCond)))
//            ;
//        }
//    }
//
//    @Nested
//    @DisplayName("모임조회-단건")
//    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
//    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
//    class 모임단건조회 {
//
//        @Test
//        @DisplayName("정상적으로 조회될 경우")
//        public void 정상_흐름() throws Exception {
//            // given
//
//            mockMvc.perform(get("/meetings/{meetingId}", 10)
//                            .header("Authorization", sampleToken)
//                    )
//                    .andExpect(status().isOk())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andDo(document("meeting-detail-normal",
//                            preprocessRequest(prettyPrint()),
//                            preprocessResponse(prettyPrint()),
//                            responseFields(beneathPath("data").withSubsectionId("data"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임의 ID"),
//                                    fieldWithPath("myMeetingUserId").type(JsonFieldType.NUMBER).description("해당 모임에서 요청을 보낸 회원의 ID"),
//                                    fieldWithPath("myMeetingRole").type(JsonFieldType.STRING).description("해당 모임에서 요청을 보낸 회원의 역할"),
//                                    fieldWithPath("title").type(JsonFieldType.STRING).description("모임의 제목"),
//                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("모임의 시작일").attributes(key("format").value("yyyy-MM-dd")),
//                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("모임의 종료일").attributes(key("format").value("yyyy-MM-dd")),
//                                    subsectionWithPath("meetingUsers").type(JsonFieldType.ARRAY).description("모임에 소속된 회원들"),
//                                    subsectionWithPath("meetingDates").type(JsonFieldType.ARRAY).description("모임에서 선택된 날짜들"),
//                                    subsectionWithPath("meetingPlaces").type(JsonFieldType.ARRAY).description("모임의 방문 장소들")
//                            ),
//                            responseFields(beneathPath("data.meetingUsers.[]").withSubsectionId("meeting-users"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 회원의 ID"),
//                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("모임 회원의 닉네임"),
//                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("모임 회원의 프로필 이미지 링크"),
//                                    fieldWithPath("meetingRole").type(JsonFieldType.STRING).description("모임 회원의 역할").attributes(key("format").value("HOST, PARTICIPANT"))
//                            ),
//                            responseFields(beneathPath("data.meetingDates.[]").withSubsectionId("meeting-dates"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 날짜의 ID"),
//                                    fieldWithPath("date").type(JsonFieldType.STRING).description("모임 날짜의 날짜").attributes(key("format").value("yyyy-MM-dd")),
//                                    fieldWithPath("userCount").type(JsonFieldType.NUMBER).description("해당 모임 날짜를 선택한 유저 수"),
//                                    fieldWithPath("dateStatus").type(JsonFieldType.STRING).description("해당 모임 날짜의 확정 여부").attributes(key("format").value("FIXED, UNFIXED"))
//                            ),
//                            responseFields(beneathPath("data.meetingPlaces.[]").withSubsectionId("meeting-places"),
//                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임 장소의 ID"),
//                                    fieldWithPath("name").type(JsonFieldType.STRING).description("모임 장소의 이름"),
//                                    fieldWithPath("memo").type(JsonFieldType.STRING).description("모임 장소의 메모"),
//                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("모임 장소의 위도"),
//                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("모임 장소의 경도"),
//                                    fieldWithPath("order").type(JsonFieldType.NUMBER).description("모임 장소의 순서")
//                            ))
//                    )
//            ;
//        }
//
//        @Test
//        @DisplayName("없는 모임 리소스를 조회하려고 할 경우")
//        public void 경로변수_예외() throws Exception {
//            // given
//
//            mockMvc.perform(get("/meetings/{meetingId}", 5)
//                            .header("Authorization", sampleToken)
//                    )
//                    .andExpect(status().isNotFound())
//                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
//                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
//                    .andDo(document("meeting-detail-error",
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
}