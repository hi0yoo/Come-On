package com.comeon.meetingservice.web.restdocs;

import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.EnumType;
import com.comeon.meetingservice.web.common.response.ErrorCode;
import com.comeon.meetingservice.web.restdocs.util.CommonResponseFieldsSnippet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
public class CommonDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void commons() throws Exception {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getInstruction));
        FieldDescriptor[] errorCodeDescriptors = errorCodes.entrySet().stream()
                .map(x -> fieldWithPath(String.valueOf(x.getKey())).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);

        mockMvc.perform(
                RestDocumentationRequestBuilders.get("/docs")

        )
                .andExpect(status().isOk())
                .andDo(document("common",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        commonResponseFields("common-response", null,
                                attributes(new Attributes.Attribute("title", "공통 응답 스펙")),
                                fieldWithPath("responseTime").type(JsonFieldType.STRING).description("응답 시간을 반환합니다."),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드를 반환합니다."),
                                subsectionWithPath("data").description("결과 데이터를 반환합니다.")
                        ),
                        commonResponseFields("common-response", beneathPath("data.apiResponseCodes").withSubsectionId("apiResponseCodes"),
                                attributes(new Attributes.Attribute("title", "응답 코드")),
                                enumConvertFieldDescriptor(getDocs(ApiResponseCode.values()))
                        ),
                        commonResponseFields("common-response", beneathPath("data.errorCodes").withSubsectionId("errorCodes"),
                                attributes(new Attributes.Attribute("title", "예외 코드")),
                                errorCodeDescriptors)

                ));
    }

    @Test
    public void error() throws Exception {

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/docs/error")
        )
                .andExpect(status().isBadRequest())
                .andDo(document("common-error",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        commonResponseFields("common-response", beneathPath("data").withSubsectionId("data"),
                                attributes(new Attributes.Attribute("title", "예외 응답 스펙")),
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 내부에서 지정한 예외 코드를 반환합니다."),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지를 반환합니다.")
                        )
                ));
    }

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {

        return enumValues.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }

    private Map<String, String> getDocs(EnumType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .collect(Collectors.toMap(EnumType::getId, EnumType::getText));
    }

    public static CommonResponseFieldsSnippet commonResponseFields(String type,
                                                                   PayloadSubsectionExtractor<?> subsectionExtractor,
                                                                   Map<String, Object> attributes, FieldDescriptor... descriptors) {
        return new CommonResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes
                , true);
    }
}