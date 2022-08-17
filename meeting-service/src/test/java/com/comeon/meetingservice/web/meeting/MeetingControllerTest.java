package com.comeon.meetingservice.web.meeting;

import org.apache.http.entity.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;

import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
class MeetingControllerTest {

    @Autowired
    MockMvc mockMvc;

    String sampleToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEsIm5hbWUiOiJ0ZXN0Iiw" +
            "iaWF0IjoxNTE2MjM5MDIyfQ.0u81Gd1qz_yiMpa3WFfCQRKNdGx3OPiMCLm4ceBgbFw";

    @Nested
    @DisplayName("모임 저장")
    class 모임저장 {
        @Test
        @DisplayName("모든 필수 데이터가 넘어온 경우")
        public void 모임_저장_정상흐름() throws Exception {
            File file = ResourceUtils.getFile(this.getClass().getResource("/static/testimage/test.png"));
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "image.png",
                    ContentType.IMAGE_PNG.getMimeType(),
                    new FileInputStream(file));

            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings")
                            .file(image)
                            .param("title", "타이틀")
                            .param("startDate", "2022-06-10")
                            .param("endDate", "2022-07-10")
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isCreated())
                    .andDo(document("meeting-post-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
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
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("생성된 모임의 ID")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("필수 데이터가 넘어오지 않은 경우")
        public void 모임_저장_파라미터예외() throws Exception {

            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings")
                            .param("title", "타이틀")
                            .header("Authorization", sampleToken)
                    )
                    .andExpect(status().isBadRequest())
                    .andDo(document("meeting-post-badrequest",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("title").description("모임 제목").ignored(),
                                    parameterWithName("courseId").description("장소를 참조할 코스의 ID").optional()
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("link:common/error-codes.html[예외 코드 참고,role=\"popup\"]"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("어떤 파라미터가 넘어오지 않았는지 표시")
                            ))
                    )
            ;
        }
        //TODO - 코스 서비스 개발 후 코스와 연동하여 장소데이터를 가져오지 못할 경우 테스트케이스 작성
    }

    @Nested
    @DisplayName("모임 수정")
    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
    class 모임수정 {

        @Test
        @DisplayName("이미지를 포함하여 수정할 경우")
        public void 이미지_포함() throws Exception {
            // given
            File file = ResourceUtils.getFile(this.getClass().getResource("/static/testimage/test.png"));
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "image.png",
                    ContentType.IMAGE_PNG.getMimeType(),
                    new FileInputStream(file));

            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings/{meetingId}", 10)
                            .file(image)
                            .param("title", "타이틀 변경")
                            .param("startDate", "2022-06-10")
                            .param("endDate", "2022-07-10")
                            .header("Authorization", sampleToken)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                    )
                    .andExpect(status().isOk())
                    .andDo(document("meeting-patch-normal-includeimage",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("title").description("수정할 모임 제목"),
                                    parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ),
                            requestParts(
                                    partWithName("image").description("수정할 모임 이미지")
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("수정된 모임의 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("수정된 모임 제목"),
                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("수정된 모임 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("수정된 모임 종료일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("storedFileName").type(JsonFieldType.STRING).description("수정된 모임 파일이 서버에 저장된 이름")

                            ))
                    )
            ;
        }

        @Test
        @DisplayName("이미지를 포함하여 수정할 경우")
        public void 이미지_미포함() throws Exception {
            // given
            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings/{meetingId}", 10)
                            .param("title", "타이틀 변경")
                            .param("startDate", "2022-06-10")
                            .param("endDate", "2022-07-10")
                            .header("Authorization", sampleToken)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                    )
                    .andExpect(status().isOk())
                    .andDo(document("meeting-patch-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("title").description("수정할 모임 제목"),
                                    parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("수정된 모임의 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("수정된 모임 제목"),
                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("수정된 모임 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("수정된 모임 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("수정에 필요한 필수 데이터를 보내지 않은 경우")
        public void 필수값_예외() throws Exception {
            // given

            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings/{meetingId}", 10)
                            .param("endDate", "2022-07-10")
                            .header("Authorization", sampleToken)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                    )
                    .andExpect(status().isBadRequest())
                    .andDo(document("meeting-patch-error-param",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("link:common/error-codes.html[예외 코드 참고,role=\"popup\"]"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("어떤 파라미터가 넘어오지 않았는지 표시")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 모임 리소스를 수정하려고 할 경우")
        public void 경로변수_예외() throws Exception {
            // given

            mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings/{meetingId}", 5)
                            .param("title", "타이틀 변경")
                            .param("startDate", "2022-06-10")
                            .param("endDate", "2022-07-10")
                            .header("Authorization", sampleToken)
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            })
                    )
                    .andExpect(status().isBadRequest())
                    .andDo(document("meeting-patch-error-pathvariable",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("title").description("수정할 모임 제목"),
                                    parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("link:common/error-codes.html[예외 코드 참고,role=\"popup\"]"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }
    }

    @Nested
    @DisplayName("모임 삭제")
    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
    class 모임삭제 {

        @Test
        @DisplayName("정상적으로 삭제될 경우")
        public void 정상_흐름() throws Exception {
            // given

            mockMvc.perform(RestDocumentationRequestBuilders.delete("/meetings/{meetingId}", 10)
                            .header("Authorization", sampleToken))
                    .andExpect(status().isOk())
                    .andDo(document("meeting-delete-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("탈퇴한 모임의 ID"),
                                    fieldWithPath("userId").type(JsonFieldType.NUMBER).description("탈퇴한 회원의 ID")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("없는 모임 리소스를 탈퇴하려고 할 경우")
        public void 경로변수_예외() throws Exception {
            // given

            mockMvc.perform(RestDocumentationRequestBuilders.delete("/meetings/{meetingId}", 5)
                            .header("Authorization", sampleToken))
                    .andExpect(status().isBadRequest())
                    .andDo(document("meeting-delete-error-pathvariable",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("link:common/error-codes.html[예외 코드 참고,role=\"popup\"]"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("모임에 속해있지 않는 회원이 탈퇴요청을 보낼 경우")
        public void 회원식별자_예외() throws Exception {
            // given

            String invalidToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEwLCJuYW1lIjo" +
                    "iSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.Dd5xvXJhuMekBRlWM8fmdLZjqCUEj_K1dpIvZMaj8-w";

            mockMvc.perform(RestDocumentationRequestBuilders.delete("/meetings/{meetingId}", 10)
                            .header("Authorization", invalidToken))
                    .andExpect(status().isBadRequest())
                    .andDo(document("meeting-delete-error-userid",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("link:common/error-codes.html[예외 코드 참고,role=\"popup\"]"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }
    }

    @Nested
    @DisplayName("모임조회-리스트")
    @Sql(value = "classpath:static/test-dml/meeting-insert.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(value = "classpath:static/test-dml/meeting-delete.sql", executionPhase = AFTER_TEST_METHOD)
    class 모임리스트조회 {

        @Test
        @DisplayName("정상적으로 조회될 경우")
        public void 정상_흐름() throws Exception {
            // given

            mockMvc.perform(RestDocumentationRequestBuilders.get("/meetings")
                            .header("Authorization", sampleToken)
                            .queryParam("page", "0")
                            .queryParam("size", "5")
                            .queryParam("title", "tle")
                            .queryParam("startDate", "2022-07-10")
                            .queryParam("endDate", "2022-08-10")
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.currentSlice").exists())
                    .andExpect(jsonPath("$.data.sizePerSlice").exists())
                    .andExpect(jsonPath("$.data.numberOfElements").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.first").exists())
                    .andExpect(jsonPath("$.data.last").exists())

                    .andDo(document("meeting-list-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("page").description("조회할 페이지(슬라이스), 기본값: 0").optional(),
                                    parameterWithName("size").description("한 페이지(슬라이스)당 조회할 데이터 수, 기본값: 5").optional(),
                                    parameterWithName("title").description("검색할 모임 제목, 해당 제목이 포함되어 있는 데이터를 조회").optional(),
                                    parameterWithName("startDate").description("검색할 시작일, 모임 시작일이 해당 날짜와 같거나 이후 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional(),
                                    parameterWithName("endDate").description("검색할 종료일, 모임 종료일이 해당 날짜와 같거나 이전 데이터만을 조회").attributes(key("format").value("yyyy-MM-dd")).optional()
                            ),
                            responseFields(beneathPath("data.contents").withSubsectionId("contents"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("모임의 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("모임의 제목"),
                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("모임의 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("모임의 종료일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("imageLink").type(JsonFieldType.STRING).description("모임의 이미지 링크"),
                                    fieldWithPath("meetingCodeId").type(JsonFieldType.NUMBER).description("모임의 모임코드 ID")
                            ))
                    )
            ;
        }

        @Test
        @DisplayName("시작일 조건을 줄 경우 조건과 같거나 조건 이후의 시작일을 가진 데이터만 검색된다.")
        public void 조건_시작일() throws Exception {
            // given
            String startDateCond = "2022-07-25";

            mockMvc.perform(RestDocumentationRequestBuilders.get("/meetings")
                            .header("Authorization", sampleToken)
                            .queryParam("startDate", startDateCond)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.contents[0].startDate", greaterThanOrEqualTo(startDateCond)))
            ;
        }

        @Test
        @DisplayName("종료일 조건을 줄 경우 조건과 같거나 조건 이전의 종료일을 가진 데이터만 검색된다.")
        public void 조건_종료일() throws Exception {
            // given
            String endDateCond = "2022-07-25";

            mockMvc.perform(RestDocumentationRequestBuilders.get("/meetings")
                            .header("Authorization", sampleToken)
                            .queryParam("endDate", endDateCond)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.contents[0].endDate", lessThanOrEqualTo(endDateCond)))
            ;
        }

        @Test
        @DisplayName("제목 조건을 줄 경우 해당 조건이 제목에 포함된 데이터만 검색된다")
        public void 조건_제목() throws Exception {
            // given
            String titleCond = "2";

            mockMvc.perform(RestDocumentationRequestBuilders.get("/meetings")
                            .header("Authorization", sampleToken)
                            .queryParam("title", titleCond)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.contents[0].title", containsString(titleCond)))
            ;
        }
    }
}