package com.comeon.meetingservice.web.meetinguser;

import com.comeon.meetingservice.web.ControllerTest;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserAddRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MeetingUserControllerTest extends ControllerTest {

    @Nested
    @DisplayName("모임유저 저장")
    class 모임유저저장 {

        private String id10Token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMCIsIm5hbWUiOiJK" +
                "b2huIERvZSIsImlhdCI6MTUxNjIzOTAyMn0.6y2nYnApKiI51dbL29a2TvaTfh6NAT9h-7UWL2RQfKM";
        private String validCode = "DG055R";
        private String expiredCode = "4G235A";
        private String nonexistentCode = "AAAAAA";

        @Test
        @DisplayName("유효한 코드와 유효한 회원을 등록할 경우 Created를 반환한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 정상_흐름() throws Exception {

            MeetingUserAddRequest meetingUserAddRequest =
                    MeetingUserAddRequest.builder()
                                    .inviteCode(validCode)
                                    .build();

            mockMvc.perform(post("/meeting-users")
                            .header("Authorization", id10Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingUserAddRequest))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("user-create-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("inviteCode").description("모임의 초대 코드")
                                            .attributes(new Attributes.Attribute(
                                                    "format",
                                                    "[영문 대문자], [숫자], [영문 대문자 + 숫자 조합] 문자열 6자리"))
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("유효기간이 지난 코드인 경우 예외가 발생한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 유효기간_예외() throws Exception {

            MeetingUserAddRequest meetingUserAddRequest =
                    MeetingUserAddRequest.builder()
                            .inviteCode(expiredCode)
                            .build();

            mockMvc.perform(post("/meeting-users")
                            .header("Authorization", id10Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingUserAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("user-create-error-expired",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("inviteCode").description("모임의 초대 코드")
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("해당 코드를 가진 모임이 없는 경우 예외가 발생한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 없는초대코드_예외() throws Exception {

            MeetingUserAddRequest meetingUserAddRequest =
                    MeetingUserAddRequest.builder()
                            .inviteCode(nonexistentCode)
                            .build();

            mockMvc.perform(post("/meeting-users")
                            .header("Authorization", id10Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingUserAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("user-create-error-nonexistent",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("inviteCode").description("모임의 초대 코드")
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("이미 모임에 가입된 회원인 경우 예외가 발생한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 회원_예외() throws Exception {

            MeetingUserAddRequest meetingUserAddRequest =
                    MeetingUserAddRequest.builder()
                            .inviteCode(validCode)
                            .build();

            mockMvc.perform(post("/meeting-users")
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingUserAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("user-create-error-user",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("inviteCode").description("모임의 초대 코드")
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("모임 초대 코드가 없거나 형식에 맞지 않으면 예외가 발생한다.")
        @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
        public void 필수값_예외() throws Exception {

            MeetingUserAddRequest meetingUserAddRequest =
                    MeetingUserAddRequest.builder()
                            .inviteCode("AA")
                            .build();

            mockMvc.perform(post("/meeting-users")
                            .header("Authorization", sampleToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson(meetingUserAddRequest))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andDo(document("user-create-error-param",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("inviteCode").description("모임의 초대 코드")
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(errorCodeLink),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                    fieldWithPath("message.inviteCode").type(JsonFieldType.ARRAY).description("검증에 실패한 이유")
                            ))
                    )
            ;
        }
    }

}