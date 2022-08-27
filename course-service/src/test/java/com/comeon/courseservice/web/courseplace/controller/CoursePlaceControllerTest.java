package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.JwtArgumentResolver;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.web.common.exception.resolver.CommonControllerAdvice;
import com.comeon.courseservice.web.courseplace.request.CoursePlaceSaveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
class CoursePlaceControllerTest {

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

    MockMvc mockMvc;

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

    @BeforeEach
    void initMockMvc(final WebApplicationContext context) throws ServletException {
        mockMvc = MockMvcBuilders.standaloneSetup(coursePlaceController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new CommonControllerAdvice())
                .setCustomArgumentResolvers(jwtArgumentResolver)
                .build();
    }

    String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

    @Nested
    @DisplayName("코스 장소 등록")
    class coursePlaceSave {

        @Test
        @DisplayName("요청 데이터 검증에 성공하면, 해당 코스에 장소를 저장한다.")
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
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면, http status 400 반환하고, 검증 오류 필드가 담긴다.")
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