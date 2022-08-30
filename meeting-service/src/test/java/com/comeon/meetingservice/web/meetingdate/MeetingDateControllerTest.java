package com.comeon.meetingservice.web.meetingdate;

import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateAddRequest;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateModifyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeetingDateControllerTest extends ControllerTest {

    String selectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjIsIm5hbWUiOiJKb2huIERvZ" +
            "SIsImlhdCI6MTUxNjIzOTAyMn0.RPxUhKwz-RU-s0qmttmh2QoP3j1pU-EUnAX74B94nD8";

    String notJoinedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEwMCwibmFtZSI6IkpvaG4gRG" +
            "9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XdpobbYZskeuceElG_LNbbstM9w-N2SYNSZdvQa7a-c";

    @Nested
    @DisplayName("모임날짜 저장")
    class 모임날짜저장 {

        @Test
        @DisplayName("모든 필수 데이터가 넘어온 경우 Created코드와 저장된, 혹은 영향을 받은 ID를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .date(LocalDate.of(2022, 07, 15))
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 10)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("date-create-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("날짜가 모임 기간내에 없다면 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 날짜_예외() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .date(LocalDate.of(2022, 8, 15))
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 10)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.DATE_NOT_WITHIN_PERIOD.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.DATE_NOT_WITHIN_PERIOD.getMessage())))
                    .andDo(document("date-create-error-date",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
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
        @DisplayName("날짜를 등록하려는 모임이 없는 경우 NotFound와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 모임_예외() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .date(LocalDate.of(2022, 07, 15))
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 5)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
                    .andDo(document("date-create-error-meetingId",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
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
        @DisplayName("이미 해당 회원이 해당 날짜를 선택했다면 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 중복_예외() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .date(LocalDate.of(2022, 07, 20))
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 10)
                            .header("Authorization", selectedToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.USER_ALREADY_SELECT.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.USER_ALREADY_SELECT.getMessage())))
                    .andDo(document("date-create-error-duplicate",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
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
        @DisplayName("해당 회원이 모임에 가입되어있지 않다면 Forbidden 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 미가입_예외() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .date(LocalDate.of(2022, 07, 15))
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 10)
                            .header("Authorization", notJoinedToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.MEETING_USER_NOT_INCLUDE.getMessage())))
                    .andDo(document("date-create-error-notjoined",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
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
        @DisplayName("필수 데이터가 없다면 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 필수값_예외() throws Exception {

            MeetingDateAddRequest meetingDateAddRequest =
                    MeetingDateAddRequest.builder()
                            .build();

            mockMvc.perform(post("/meetings/{meetingId}/dates", 10)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))
                    .andDo(document("date-create-error-param",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("date").description("추가할 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                    fieldWithPath("message.date").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                            ))
                    )
            ;
        }
    }

    @Nested
    @DisplayName("모임날짜 수정")
    class 모임날짜수정 {

        @Test
        @DisplayName("모든 필수 데이터가 넘어오고 형식이 맞다면 성공적으로 수정된다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            MeetingDateModifyRequest meetingDateModifyRequest =
                    MeetingDateModifyRequest.builder()
                            .dateStatus(DateStatus.FIXED)
                            .build();

            mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", 10, 10)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateModifyRequest))
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("date-modify-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(new Attributes.Attribute("format", "FIXED, UNFIXED"))
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 날짜를 수정할 경우 NotFound와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 식별자_예외() throws Exception {

            MeetingDateModifyRequest meetingDateModifyRequest =
                    MeetingDateModifyRequest.builder()
                            .dateStatus(DateStatus.FIXED)
                            .build();

            mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", 10, 5)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingDateModifyRequest))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
                    .andDo(document("date-modify-error-pathvariable",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(new Attributes.Attribute("format", "FIXED, UNFIXED"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("DateStatus 형식에 맞지 않으면 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 형식_예외() throws Exception {

            Map<String, String> modifyDummyRequest = new HashMap<>();
            modifyDummyRequest.put("dateStatus", "xxx");

            mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", 10, 5)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(modifyDummyRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage())))
                    .andDo(document("date-modify-error-format",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("dateStatus").description("변경할 날짜의 상태").attributes(new Attributes.Attribute("format", "FIXED, UNFIXED"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("필수 데이터가 넘어오지 않는다면 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 필수값_예외() throws Exception {

            mockMvc.perform(patch("/meetings/{meetingId}/dates/{dateId}", 10, 5)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.VALIDATION_FAIL.getCode())))
                    .andDo(document("date-modify-error-param",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                    fieldWithPath("message.dateStatus").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")

                            ))
                    )
            ;
        }

    }

    @Nested
    @DisplayName("모임날짜 삭제")
    class 모임날짜삭제{

        @Test
        @DisplayName("경로변수와 회원ID 값이 유효하다면 정상적으로 삭제된다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 10)
                            .header("Authorization", selectedToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("date-delete-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint())
                    ))
            ;
        }

        @Test
        @DisplayName("회원이 해당 날짜를 선택하지 않은 경우 BadRequest와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 회원_예외() throws Exception {

            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 10)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.USER_NOT_SELECT_DATE.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.USER_NOT_SELECT_DATE.getMessage())))
                    .andDo(document("date-delete-error-user",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 날짜 ID로 요청을 보낼 경우 NotFound와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 식별자_예외() throws Exception {

            mockMvc.perform(delete("/meetings/{meetingId}/dates/{dateId}", 10, 5)
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
                    .andDo(document("date-delete-error-pathvariable",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }
    }

    @Nested
    @DisplayName("모임날짜조회 - 단건")
    class 모임날짜단건조회 {

        @Test
        @DisplayName("날짜 ID가 정상적이라면 날짜와 날짜를 선택한 회원 정보들을 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            mockMvc.perform(get("/meetings/{meetingId}/dates/{dateId}", 10, 10)
                            .header("Authorization", selectedToken)
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("date-detail-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("조회된 모임 날짜의 ID"),
                                    fieldWithPath("date").type(JsonFieldType.STRING).description("모임 날짜의 날짜").attributes(new Attributes.Attribute("format", "yyyy-MM-dd")),
                                    fieldWithPath("userCount").type(JsonFieldType.NUMBER).description("해당 날짜를 선택한 회원 수"),
                                    fieldWithPath("dateStatus").type(JsonFieldType.STRING).description("해당 날짜의 확정 여부").attributes(new Attributes.Attribute("format", "FIXED, UNFIXED")),
                                    subsectionWithPath("dateUsers").type(JsonFieldType.ARRAY).description("해당 날짜를 선택한 회원들의 정보")
                            ),
                            responseFields(beneathPath("data.dateUsers.[]").withSubsectionId("date-users"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("해당 날짜를 선택한 회원의 모임 회원 ID"),
                                    fieldWithPath("nickname").type(JsonFieldType.STRING).description("해당 모임 회원의 닉네임"),
                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("해당 모임 회원의 프로필 이미지 링크"),
                                    fieldWithPath("meetingRole").type(JsonFieldType.STRING).description("해당 모임 회원의 역할").attributes(key("format").value("HOST, PARTICIPANT"))
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 모임날짜 리소스를 조회하려고 할 경우 NotFound와 예외 정보를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 경로변수_예외() throws Exception {

            mockMvc.perform(get("/meetings/{meetingId}/dates/{dateId}", 10, 5)
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.code", equalTo(ErrorCode.ENTITY_NOT_FOUND.getCode())))
                    .andExpect(jsonPath("$.data.message", equalTo(ErrorCode.ENTITY_NOT_FOUND.getMessage())))
                    .andDo(document("date-detail-error-pathvariable",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }
    }
}