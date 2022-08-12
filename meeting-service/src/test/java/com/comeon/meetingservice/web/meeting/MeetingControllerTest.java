package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.util.ValidationUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class MeetingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void 여행_저장_정상흐름() throws Exception {
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
                        .header("Authorization",
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEsIm5hbWUiOiJ0ZXN0Iiw" +
                                        "iaWF0IjoxNTE2MjM5MDIyfQ.0u81Gd1qz_yiMpa3WFfCQRKNdGx3OPiMCLm4ceBgbFw")
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
    public void 여행_저장_파라미터예외() throws Exception {

        mockMvc.perform(RestDocumentationRequestBuilders.multipart("/meetings")
                        .param("title", "타이틀")
                        .header("Authorization",
                                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOjEsIm5hbWUiOiJ0ZXN0Iiw" +
                                        "iaWF0IjoxNTE2MjM5MDIyfQ.0u81Gd1qz_yiMpa3WFfCQRKNdGx3OPiMCLm4ceBgbFw")
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