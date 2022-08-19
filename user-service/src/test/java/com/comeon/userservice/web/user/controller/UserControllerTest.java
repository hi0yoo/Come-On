package com.comeon.userservice.web.user.controller;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import com.comeon.userservice.domain.user.repository.UserRepository;
import com.comeon.userservice.web.common.exception.resolver.CommonControllerAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Transactional
@SpringBootTest
class UserControllerTest {

    @Autowired
    UserController userController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void initMockMvc(final WebApplicationContext context) throws ServletException {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new CommonControllerAdvice())
                .build();
    }

    @Nested
    @DisplayName("회원 등록")
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
        @DisplayName("success - 요청 데이터 검증에 성공하면 회원 정보를 저장하고 userId와 role을 반환한다.")
        void userSave_success_1() throws Exception {
            // given
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1@email.com";
            profileImgUrl = "profileImgUrl";

            // when
            Map<String, Object> requestBody = generateRequestBody();
            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            User user = userRepository.findByOAuthIdAndProvider(
                    oauthId,
                    OAuthProvider.valueOf(provider)
            ).orElseThrow();

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$..data[?(@.userId)]").exists())
                    .andExpect(jsonPath("$..data[?(@.userId)]").isNotEmpty())
                    .andExpect(jsonPath("$..data[?(@.userId == '%d')]", user.getId()).exists())
                    .andExpect(jsonPath("$..data[?(@.role)]").exists())
                    .andExpect(jsonPath("$..data[?(@.role)]").isNotEmpty())
                    .andExpect(jsonPath("$..data[?(@.role == '%s')]", user.getRole().getRoleValue()).exists());
        }

        @Test
        @DisplayName("success - profileImgUrl은 선택값으로 null을 허용한다.")
        void userSave_success_2() throws Exception {
            // given
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1@email.com";

            // when
            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            User user = userRepository.findByOAuthIdAndProvider(
                    oauthId,
                    OAuthProvider.valueOf(provider)
            ).orElseThrow();

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$..data[?(@.userId)]").exists())
                    .andExpect(jsonPath("$..data[?(@.userId)]").isNotEmpty())
                    .andExpect(jsonPath("$..data[?(@.userId == '%d')]", user.getId()).exists())
                    .andExpect(jsonPath("$..data[?(@.role)]").exists())
                    .andExpect(jsonPath("$..data[?(@.role)]").isNotEmpty())
                    .andExpect(jsonPath("$..data[?(@.role == '%s')]", user.getRole().getRoleValue()).exists());
        }

        @Test
        @DisplayName("fail - email이 유효한 형식이 아니면 검증이 실패하여 http status 400 오류를 반환한다.")
        void userSave_fail_1() throws Exception {
            // given
            oauthId = "12345";
            provider = "kakao".toUpperCase();
            name = "testName1";
            email = "email1";

            // when
            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - 등록되지 않은 Provider가 요청으로 넘어오면, 검증이 실패하고 http status 400 오류를 반환한다.")
        void userSave_fail_2() throws Exception {
            oauthId = "12345";
            provider = "daum".toUpperCase();
            name = "testName1";
            email = "email1";

            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("fail - oauthId, provider, email, name은 필수값으로, 입력하지 않으면 http status 400 오류를 반환한다.")
        void userSave_fail_3() throws Exception {
            Map<String, Object> requestBody = generateRequestBody();

            ResultActions perform = mockMvc.perform(
                    post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(requestBody))
            );

            perform.andExpect(status().isBadRequest());
        }
    }
}