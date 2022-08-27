package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.JwtArgumentResolver;
import com.comeon.courseservice.docs.config.RestDocsSupport;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.web.courseplace.request.CoursePlaceSaveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
class CoursePlaceControllerTest extends RestDocsSupport {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CoursePlaceRepository coursePlaceRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtArgumentResolver jwtArgumentResolver;

    @Autowired
    CoursePlaceController coursePlaceController;

    Course course;

    void initCourse() {
        Long userId = 1L;
        String title = "courseTitle";
        String description = "courseDescription";
        CourseImage courseImage = CourseImage.builder()
                .originalName("originalName")
                .storedName("storedName")
                .build();

        Course courseToSave = Course.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .courseImage(courseImage)
                .build();
        course = courseRepository.save(courseToSave);
    }

    String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

    @Nested
    @DisplayName("코스 장소 등록")
    class coursePlaceSave {

        @Test
        @DisplayName("[docs] 요청 데이터 검증에 성공하면, 해당 코스에 장소를 저장한다.")
        void success() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();
            String name = "placeName";
            String description = "placeDescription";
            Double lat = 12.34;
            Double lng = 34.56;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(courseId, name, description, lat, lng);

            Long userId = course.getUserId();
            String userRole = "ROLE_USER";
            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", userRole)
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/course-places")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coursePlaceId").exists());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("대상 코스의 식별값"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("장소의 설명"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("장소의 경도값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("coursePlaceId").type(JsonFieldType.NUMBER).description("저장된 코스 장소 식별값")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 요청 데이터 검증에 실패하면, http status 400 반환하고, 검증 오류 필드가 담긴다.")
        void fail() throws Exception {
            // given
            initCourse();

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest();

            Long userId = course.getUserId();
            String userRole = "ROLE_USER";
            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", userRole)
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/course-places")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());
            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message.courseId").exists())
                    .andExpect(jsonPath("$.data.message.name").exists())
                    .andExpect(jsonPath("$.data.message.description").exists())
                    .andExpect(jsonPath("$.data.message.lat").exists())
                    .andExpect(jsonPath("$.data.message.lng").exists());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("예외 코드"),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지"),
                                    fieldWithPath("message.courseId").ignored(),
                                    fieldWithPath("message.name").ignored(),
                                    fieldWithPath("message.description").ignored(),
                                    fieldWithPath("message.lat").ignored(),
                                    fieldWithPath("message.lng").ignored()
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에는 성공했으나, 저장되지 않은 코스의 식별자가 넘어오면, http status 400 반환한다.")
        void fail2() throws Exception {
            // given
            initCourse();
            Long courseId = 100L;
            String name = "placeName";
            String description = "placeDescription";
            Double lat = 12.34;
            Double lng = 34.56;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(courseId, name, description, lat, lng);

            Long userId = course.getUserId();
            String userRole = "ROLE_USER";
            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", userRole)
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(userId.toString())
                    .compact();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/course-places")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("요청 데이터 검증에는 성공했으나, 코스를 등록한 유저가 아니면, http status 403 반환한다.")
        void fail3() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();
            String name = "placeName";
            String description = "placeDescription";
            Double lat = 12.34;
            Double lng = 34.56;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(courseId, name, description, lat, lng);

            Long invalidUserId = 100L;
            String userRole = "ROLE_USER";
            String accessToken = Jwts.builder()
                    .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                    .claim("auth", userRole)
                    .setIssuer("test")
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                    .setSubject(invalidUserId.toString())
                    .compact();

            // when
            ResultActions perform = mockMvc.perform(
                    post("/course-places")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isForbidden());
        }
    }
}