package com.comeon.meetingservice.web.meetingplace;

import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceSaveRequest;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeetingPlaceControllerTest extends ControllerTest {

    @Nested
    @DisplayName("모임장소 저장")
    class 모임장소저장 {

        @Test
        @DisplayName("모든 필수 데이터가 넘어온 경우 Created코드와 저장된 ID를 응답한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            MeetingPlaceSaveRequest meetingPlaceSaveRequest =
                    MeetingPlaceSaveRequest.builder()
                            .meetingId(10L)
                            .name("모임장소이름")
                            .lat(1.1)
                            .lng(1.1)
                            .build();

            mockMvc.perform(RestDocumentationRequestBuilders.post("/meeting-places")
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingPlaceSaveRequest))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-create-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("meetingId").description("장소를 추가할 모임의 ID"),
                                    fieldWithPath("name").description("추가할 장소의 이름"),
                                    fieldWithPath("lat").description("추가할 장소의 위도"),
                                    fieldWithPath("lng").description("추가할 장소의 경도")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 모임 ID일 경우 BadRequest와 예외 정보가 응답된다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 파라미터_예외() throws Exception {

            MeetingPlaceSaveRequest meetingPlaceSaveRequest =
                    MeetingPlaceSaveRequest.builder()
                            .meetingId(5L)
                            .name("모임장소이름")
                            .lat(1.1)
                            .lng(1.1)
                            .build();

            mockMvc.perform(RestDocumentationRequestBuilders.post("/meeting-places")
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingPlaceSaveRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-create-error-meetingid",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("meetingId").description("장소를 추가할 모임의 ID"),
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
        @DisplayName("필수 값이 없을 경우 BadRequest와 예외 정보가 응답된다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 필수값_예외() throws Exception {

            Map<String, Long> dummyContents = new HashMap<>();
            dummyContents.put("meetingId", 10L);

            mockMvc.perform(RestDocumentationRequestBuilders.post("/meeting-places")
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(dummyContents))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-create-error-param",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("meetingId").description("장소를 추가할 모임의 ID")
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