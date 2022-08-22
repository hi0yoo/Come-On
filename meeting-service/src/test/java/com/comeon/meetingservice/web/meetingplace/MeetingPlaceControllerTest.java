package com.comeon.meetingservice.web.meetingplace;

import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceModifyRequest;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceSaveRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.RequestDocumentation;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
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

            mockMvc.perform(post("/meeting-places")
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

            mockMvc.perform(post("/meeting-places")
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

            mockMvc.perform(post("/meeting-places")
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

    @Nested
    @DisplayName("모임장소 수정")
    class 모임장소수정 {

        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            @Test
            @DisplayName("모임 장소 정보를 수정할 경우 name, lat, lng 필드가 모두 필요하다.")
            @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
            public void 모임_장소_정보_정상() throws Exception {

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name("changed name")
                                .lat(10.1)
                                .lng(10.1)
                                .build();

                mockMvc.perform(patch("/meeting-places/{meetingPlaceId}", 10)
                                .header("Authorization", sampleToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andDo(document("place-modify-normal-info",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
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
            @DisplayName("장소 메모를 수정할 경우 memo 필드만 있으면 정상적으로 수행된다.")
            @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
            public void 모임_장소_메모() throws Exception {

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .memo("changed memo")
                                .build();

                mockMvc.perform(patch("/meeting-places/{meetingPlaceId}", 10)
                                .header("Authorization", sampleToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andDo(document("place-modify-normal-memo",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
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
            @DisplayName("장소 순서를 수정할 경우 order 필드만 있으면 정상적으로 수행된다.")
            @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
            public void 모임_장소_순서() throws Exception {

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .order(2)
                                .build();

                mockMvc.perform(patch("/meeting-places/{meetingPlaceId}", 10)
                                .header("Authorization", sampleToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andDo(document("place-modify-normal-order",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
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
        @DisplayName("예외가 발생할 경우")
        class 예외 {

            @Test
            @DisplayName("모임 장소를 수정할 경우 name, lat, lng 필드 중 하나라도 없다면 예외가 발생한다.")
            @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
            public void 모임_장소_정보_예외() throws Exception {

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name("changed name")
                                .lat(10.1)
                                .build();

                mockMvc.perform(patch("/meeting-places/{meetingPlaceId}", 10)
                                .header("Authorization", sampleToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andDo(document("place-modify-error-info",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("name").description("수정할 장소의 이름"),
                                        fieldWithPath("lat").description("수정할 장소의 위도"),
                                        fieldWithPath("lng").description("수정할 장소의 경도"),
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
            @DisplayName("없는 모임 장소 ID일 경우 BadRequest와 예외 정보가 응답된다.")
            @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
            @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
            public void 경로변수_예외() throws Exception {

                MeetingPlaceModifyRequest meetingPlaceModifyRequest =
                        MeetingPlaceModifyRequest.builder()
                                .name("changed name")
                                .lat(10.1)
                                .lng(10.1)
                                .build();

                mockMvc.perform(patch("/meeting-places/{meetingPlaceId}", 5)
                                .header("Authorization", sampleToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson(meetingPlaceModifyRequest))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andDo(document("place-modify-error-pathvariable",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
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

        @Test
        @DisplayName("정상적으로 삭제될 경우")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            mockMvc.perform(delete("/meeting-places/{meetingPlaceId}", 10)
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-delete-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint())
                    ))
            ;
        }

        @Test
        @DisplayName("없는 장소를 삭제하려 할 경우")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 경로변수_예외() throws Exception {

            mockMvc.perform(delete("/meeting-places/{meetingPlaceId}", 5)
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-delete-error-pathvariable",
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
    @DisplayName("모임조회 - 단건")
    class 모임단건조회 {

        @Test
        @DisplayName("정상적으로 조회될 경우")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            mockMvc.perform(get("/meeting-places/{meetingPlaceId}", 10)
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-detail-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("모임 장소의 이름"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("모임 장소의 위도"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("모임 장소의 경도")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 장소를 조회하려 할 경우")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 경로변수_예외() throws Exception {

            mockMvc.perform(get("/meeting-places/{meetingPlaceId}", 5)
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("place-detail-error-pathvariable",
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