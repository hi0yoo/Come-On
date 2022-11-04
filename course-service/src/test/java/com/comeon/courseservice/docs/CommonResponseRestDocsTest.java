package com.comeon.courseservice.docs;

import com.comeon.courseservice.docs.config.CommonRestDocsSupport;
import com.comeon.courseservice.docs.utils.RestDocsUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class CommonResponseRestDocsTest extends CommonRestDocsSupport {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("API 공통 응답")
    void commonResponse() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/success").accept(MediaType.APPLICATION_JSON)
        );

        Map<String, String> data = (Map<String, String>) objectMapper
                .readValue(perform.andReturn()
                                .getResponse()
                                .getContentAsByteArray(),
                        new TypeReference<Map<String, Object>>() {}
                )
                .get("data");

        perform.andExpect(status().isOk())
                .andDo(
                        restDocs.document(
                                RestDocsUtil.customResponseFields(
                                        "common-response", null,
                                        attributes(key("title").value("공통 응답 스펙")),
                                        fieldWithPath("responseTime").description("응답 시간을 반환합니다."),
                                        fieldWithPath("code").description("응답 코드를 반환합니다."),
                                        subsectionWithPath("data").description("결과 데이터를 반환합니다.")
                                ),
                                RestDocsUtil.customResponseFields(
                                        "common-response", beneathPath("data").withSubsectionId("code"),
                                        attributes(key("title").value("응답 코드")),
                                        enumConvertFieldDescriptor(data)
                                )
                        )
                );
    }

    @Test
    @DisplayName("API 예외 응답")
    void errorResponse() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/error").accept(MediaType.APPLICATION_JSON)
        );

        perform.andExpect(status().isBadRequest())
                .andDo(
                        restDocs.document(
                                RestDocsUtil.customResponseFields(
                                        "common-response", beneathPath("data").withSubsectionId("error"),
                                        attributes(key("title").value("예외 응답 스펙")),
                                        fieldWithPath("errorCode").description("API 내부에서 지정한 예외 코드를 반환합니다."),
                                        fieldWithPath("message").description("예외 메시지를 반환합니다.")
                                )
                        )
                );
    }

    @Test
    @DisplayName("API 예외 응답 코드")
    void errorCode() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/error/codes").accept(MediaType.APPLICATION_JSON)
        );

        Map<String, String> data = (Map<String, String>) objectMapper
                .readValue(perform.andReturn()
                                .getResponse()
                                .getContentAsByteArray(),
                        new TypeReference<Map<String, Object>>() {}
                )
                .get("data");

        perform.andDo(
                restDocs.document(
                        RestDocsUtil.customResponseFields(
                                "error-code-response", beneathPath("data").withSubsectionId("error-codes"),
                                attributes(key("title").value("예외 응답 코드")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    @Test
    @DisplayName("List 응답 형식")
    void listResponse() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/list/response").accept(MediaType.APPLICATION_JSON)
        );

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                attributes(key("title").value("응답 필드")),
                                fieldWithPath("count").type(JsonFieldType.NUMBER).description("contents 필드 내부 데이터의 총 개수"),
                                fieldWithPath("contents").type(JsonFieldType.ARRAY).description("요청에 대한 실제 응답 데이터 필드")
                        )
                )
        );
    }

    @Test
    @DisplayName("Slice 응답 형식")
    void sliceResponse() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/slice/response").accept(MediaType.APPLICATION_JSON)
        );

        perform.andDo(
                restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                attributes(key("title").value("응답 필드")),
                                fieldWithPath("currentSlice").type(JsonFieldType.NUMBER).description("현재 페이지 번호. 0부터 시작."),
                                fieldWithPath("sizePerSlice").type(JsonFieldType.NUMBER).description("한 페이지당 contents 필드 내부 데이터의 개수"),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지의 contents 필드 내부 데이터 개수"),
                                fieldWithPath("hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지의 존재 여부"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지의 존재 여부"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("처음 페이지인지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("마지막 페이지인지 여부"),
                                fieldWithPath("contents").type(JsonFieldType.ARRAY).description("요청에 대한 실제 응답 데이터 필드")
                        )
                )
        );
    }

    @Test
    @DisplayName("장소 카테고리 목록")
    void placeCategoryList() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/course-places/category/codes").accept(MediaType.APPLICATION_JSON)
        );

        Map<String, String> data = (Map<String, String>) objectMapper
                .readValue(perform.andReturn()
                                .getResponse()
                                .getContentAsByteArray(),
                        new TypeReference<Map<String, Object>>() {}
                )
                .get("data");

        perform.andDo(
                restDocs.document(
                        RestDocsUtil.customResponseFields(
                                "enum-response", beneathPath("data").withSubsectionId("place-category"),
                                attributes(key("title").value("장소 카테고리 코드 목록")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    @Test
    @DisplayName("코스 상태 목록")
    void courseStatusList() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/courses/status/codes").accept(MediaType.APPLICATION_JSON)
        );

        Map<String, String> data = (Map<String, String>) objectMapper
                .readValue(perform.andReturn()
                                .getResponse()
                                .getContentAsByteArray(),
                        new TypeReference<Map<String, Object>>() {}
                )
                .get("data");

        perform.andDo(
                restDocs.document(
                        RestDocsUtil.customResponseFields(
                                "enum-response", beneathPath("data").withSubsectionId("course-status"),
                                attributes(key("title").value("코스 상태 코드 목록")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }
}
