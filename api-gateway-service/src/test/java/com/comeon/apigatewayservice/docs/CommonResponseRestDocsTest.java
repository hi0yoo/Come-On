package com.comeon.apigatewayservice.docs;

import com.comeon.apigatewayservice.docs.utils.RestDocsUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(CommonRestDocsController.class)
public class CommonResponseRestDocsTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

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
                        document(
                                "{class-name}/{method-name}",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                responseFields(
                                        attributes(key("title").value("공통 응답 스펙")),
                                        fieldWithPath("responseTime").type(JsonFieldType.STRING).description("응답 시간을 반환합니다."),
                                        fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드를 반환합니다."),
                                        subsectionWithPath("data").type(JsonFieldType.OBJECT).description("결과 데이터를 반환합니다.")
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
    @DisplayName("API 예외 응답 코드")
    void errorCode() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/error/codes").accept(MediaType.APPLICATION_JSON)
        );

        Map<String, String> data = (Map<String, String>) objectMapper
                .readValue(perform.andReturn()
                                .getResponse()
                                .getContentAsByteArray(),
                        new TypeReference<Map<String, Object>>() {
                        }
                )
                .get("data");

        perform.andDo(
                document(
                        "{class-name}/{method-name}",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        RestDocsUtil.customResponseFields(
                                "error-code-response", beneathPath("data").withSubsectionId("error-codes"),
                                attributes(key("title").value("오류 응답 코드")),
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
                        document(
                                "{class-name}/{method-name}",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                responseFields(
                                        beneathPath("data").withSubsectionId("error"),
                                        attributes(key("title").value("예외 응답 스펙")),
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지를 반환합니다.")
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
