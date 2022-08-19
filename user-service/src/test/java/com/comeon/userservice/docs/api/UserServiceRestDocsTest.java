package com.comeon.userservice.docs.api;

import com.comeon.userservice.docs.config.RestDocsSupport;
import com.comeon.userservice.docs.utils.RestDocsUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
public class UserServiceRestDocsTest extends RestDocsSupport {

    @Nested
    @DisplayName("유저 정보 저장")
    class userSave {

        String oauthId;
        String provider;
        String email;
        String name;
        String profileImgUrl;

        Map<String, Object> generateRequestBody() {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("oauthId", oauthId);
            request.put("provider", provider);
            request.put("name", name);
            request.put("email", email);
            request.put("profileImgUrl", profileImgUrl);
            return request;
        }

        @Test
        @DisplayName("config - 지원하는 Provider 정보")
        void providers() throws Exception {
            ResultActions perform = mockMvc.perform(
                    get("/docs/providers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
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
                                            "common-response", beneathPath("data").withSubsectionId("providers"),
                                            attributes(key("title").value("Provider 목록")),
                                            enumConvertFieldDescriptor(data)
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("success - 유저 정보 저장에 성공")
        void success() throws Exception {
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isOk())
                    .andDo(
                            restDocs.document(
                                    RestDocsUtil.customRequestFields(
                                            "custom-request", null,
                                            attributes(key("title").value("요청 필드")),
                                            fieldWithPath("oauthId").type(JsonFieldType.STRING).description("OAuth 로그인 성공시, Provider에서 제공하는 유저 ID값"),
                                            fieldWithPath("provider").type(JsonFieldType.STRING).description("OAuth 유저 정보 제공자"),
                                            fieldWithPath("email").type(JsonFieldType.STRING).description("유저 이메일 정보"),
                                            fieldWithPath("name").type(JsonFieldType.STRING).description("유저 이름 또는 닉네임 정보"),
                                            fieldWithPath("profileImgUrl").type(JsonFieldType.STRING).description("유저 프로필 이미지 URL").optional()
                                    ),
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("저장된 유저의 식별값"),
                                            fieldWithPath("role").type(JsonFieldType.STRING).description("저장된 유저의 권한")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("fail - 지원하지 않는 Provider 지정한 경우")
        void failNotSupportedProvider() throws Exception {
            oauthId = "12345";
            provider = "daum".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isBadRequest())
                    .andDo(
                            restDocs.document(
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                    )
                            )
                    );
        }

        @Test
        @DisplayName("fail - 요청 데이터 검증에 실패한 경우")
        void failValid() throws Exception {
            provider = "daum".toUpperCase();
            name = "testName1";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );
            perform.andExpect(status().isBadRequest())
                    .andDo(
                            restDocs.document(
                                    responseFields(
                                            beneathPath("data").withSubsectionId("data"),
                                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                            fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                                    )
                            )
                    );
        }
    }

    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }
}
