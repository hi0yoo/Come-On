package com.comeon.userservice.docs;

import com.comeon.userservice.docs.config.CommonRestDocsSupport;
import com.comeon.userservice.docs.utils.RestDocsUtil;
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

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
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
                                        fieldWithPath("code").description("API 내부에서 지정한 예외 코드를 반환합니다."),
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
                                "common-response", beneathPath("data").withSubsectionId("error-codes"),
                                attributes(key("title").value("예외 응답 코드")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    @Test
    @DisplayName("지원하는 소셜 로그인 서비스 제공 벤더 목록")
    void providerCodes() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/providers").accept(MediaType.APPLICATION_JSON)
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
                                "common-response", beneathPath("data").withSubsectionId("provider-codes"),
                                attributes(key("title").value("지원하는 소셜 로그인 서비스 제공 벤더 목록")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    @Test
    @DisplayName("유저 상태 코드 목록")
    void userStatusCodes() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/user-status").accept(MediaType.APPLICATION_JSON)
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
                                "common-response", beneathPath("data").withSubsectionId("user-status-codes"),
                                attributes(key("title").value("유저 상태 코드 목록")),
                                enumConvertFieldDescriptor(data)
                        )
                )
        );
    }

    @Test
    @DisplayName("유저 권한 목록")
    void userRoleCodes() throws Exception {
        ResultActions perform = mockMvc.perform(
                get("/docs/user-role").accept(MediaType.APPLICATION_JSON)
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
                                "common-response", beneathPath("data").withSubsectionId("user-role-codes"),
                                attributes(key("title").value("유저 권한 목록")),
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

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }
}
