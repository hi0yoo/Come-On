package com.comeon.meetingservice.web.restdocs;

import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import com.comeon.meetingservice.web.ControllerTestBase;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.EnumType;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.restdocs.util.CommonResponseFieldsSnippet;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.restdocs.snippet.Attributes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommonDocumentationTest extends ControllerTestBase {

    @Test
    public void commons() throws Exception {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        FieldDescriptor[] errorCodeDescriptors = errorCodes.entrySet().stream()
                .map(x -> fieldWithPath(String.valueOf(x.getKey())).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);

        Map<String, String> placeCategories = Arrays.stream(PlaceCategory.values())
                .collect(Collectors.toMap(PlaceCategory::name, PlaceCategory::getKorName));
        FieldDescriptor[] placeCategoryDescriptors = placeCategories.entrySet().stream()
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
                                errorCodeDescriptors
                        ),
                        commonResponseFields("common-response", beneathPath("data.placeCategories").withSubsectionId("placeCategories"),
                                attributes(new Attributes.Attribute("title", "장소 카테고리")),
                                placeCategoryDescriptors
                        ))

                );
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

    @Test
    public void list() throws Exception {

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/docs/list")
                )
                .andExpect(status().isOk())
                .andDo(document("common-list",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        commonResponseFields("common-response", beneathPath("data").withSubsectionId("data"),
                                attributes(new Attributes.Attribute("title", "리스트 응답 스펙")),
                                fieldWithPath("currentSlice").type(JsonFieldType.NUMBER).description("현재 슬라이스 번호를 표시합니다. 0부터 시작합니다."),
                                fieldWithPath("sizePerSlice").type(JsonFieldType.NUMBER).description("한 슬라이스당 몇 개의 요소를 응답하는지 표시합니다."),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER).description("현재 슬라이스에 몇 개의 요소가 들어있는지 표시합니다."),
                                fieldWithPath("hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 슬라이스가 있는지 표시합니다."),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 슬라이스가 있는지 표시합니다."),
                                fieldWithPath("contents").type(JsonFieldType.ARRAY).description("실질적으로 요청에 따라 응답되는 데이터가 들어갑니다."),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("처음 슬라이스인지 표시합니다."),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("마지막 슬라이스인지 표시합니다.")
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