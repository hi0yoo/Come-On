package com.comeon.meetingservice.web.meeting;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                                    fieldWithPath("code").type(JsonFieldType.STRING).description("요청 데이터 검증에 실패할 경우 101 코드를 표시"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("어떤 파라미터가 넘어오지 않았는지 표시")
                            ))
                    )
            ;
        }
        //TODO - 코스 서비스 개발 후 코스와 연동하여 장소데이터를 가져오지 못할 경우 테스트케이스 작성
    }

    @Nested
    @DisplayName("모임 수정")
    @Sql(value = "classpath:./static/test-dml/meeting-modify-before.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(value = "classpath:./static/test-dml/meeting-modify-after.sql", executionPhase = AFTER_TEST_METHOD)
    class 모임수정 {

        @Test
        public void 정상_흐름() throws Exception {
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
                    .andDo(document("meeting-patch-normal",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    parameterWithName("title").description("수정할 모임 제목"),
                                    parameterWithName("startDate").description("수정할 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    parameterWithName("endDate").description("수정할 종료일").attributes(key("format").value("yyyy-MM-dd"))
                            ),
                            requestParts(
                                    partWithName("image").description("수정할 모임 이미지").optional()
                            ),
                            responseFields(beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("수정된 모임의 ID"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("수정된 모임 제목"),
                                    fieldWithPath("startDate").type(JsonFieldType.STRING).description("수정된 모임 시작일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("endDate").type(JsonFieldType.STRING).description("수정된 모임 종료일").attributes(key("format").value("yyyy-MM-dd")),
                                    fieldWithPath("storedFileName").type(JsonFieldType.STRING).description("수정된 모임 파일이 서버에 저장된 이름").optional()

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
                                    fieldWithPath("code").type(JsonFieldType.STRING).description("요청 데이터 검증에 실패할 경우 101 코드를 표시"),
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
                                    fieldWithPath("code").type(JsonFieldType.STRING).description("경로변수의 ID에 해당하는 리소스가 없을 경우 104코드 표시"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            ))
                    )
            ;
        }
    }
}