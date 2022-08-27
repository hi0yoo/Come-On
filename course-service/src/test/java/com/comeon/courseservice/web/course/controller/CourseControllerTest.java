package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.config.argresolver.JwtArgumentResolver;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseWriteStatus;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.web.common.exception.resolver.CommonControllerAdvice;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.course.response.CourseWritingDoneResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
@Import({S3MockConfig.class})
class CourseControllerTest {

    @Value("${profile.dirName}")
    String dirName;

    @Autowired
    FileManager fileManager;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CoursePlaceRepository coursePlaceRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtArgumentResolver jwtArgumentResolver;

    @Autowired
    CourseController courseController;

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
        mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setControllerAdvice(new CommonControllerAdvice())
                .setCustomArgumentResolvers(jwtArgumentResolver)
                .build();
    }

    String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

    @Nested
    @DisplayName("코스 저장")
    class courseSave {

        @Test
        @DisplayName("요청 데이터 검증에 성공하면, 코스를 저장하고, 해당 코스 식별자를 응답으로 반환한다.")
        void success() throws Exception {
            // given
            String title = "courseTitle";
            String description = "courseDescription";

            File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.jpeg"));
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imgFile",
                    "test-img.jpeg",
                    ContentType.IMAGE_JPEG.getMimeType(),
                    new FileInputStream(imgFile)
            );

            Long userId = 1L;
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
                    multipart("/courses")
                            .file(mockMultipartFile)
                            .param("title", title)
                            .param("description", description)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").exists());
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면, http status 400 반환한다.")
        void validationFail() throws Exception {
            Long userId = 1L;
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
                    multipart("/courses")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message.imgFile").exists())
                    .andExpect(jsonPath("$.data.message.description").exists())
                    .andExpect(jsonPath("$.data.message.title").exists());
        }
    }

    @Nested
    @DisplayName("코스 작성 완료 처리")
    class courseWritingDone {

        @Test
        @DisplayName("코스에 장소가 등록되어 있다면, 해당 코스의 작성 상태를 변경하고, 요청 처리 완료 메시지를 응답한다.")
        void success() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name("placeName")
                    .description("placeDescription")
                    .lat(12.34)
                    .lng(34.56)
                    .order(1)
                    .build();
            coursePlaceRepository.save(coursePlace);

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
                    patch("/courses/{courseId}/done", courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value(CourseWritingDoneResponse.SUCCESS_MESSAGE));

            Course findCourse = courseRepository.findById(courseId).orElse(null);
            assertThat(findCourse).isNotNull();
            assertThat(findCourse.getWriteStatus()).isEqualTo(CourseWriteStatus.COMPLETE);
        }

        @Test
        @DisplayName("코스에 장소가 등록되어있지 않으면, 요청이 실패하고 http status 400 반환한다.")
        void failNoPlaces() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

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
                    patch("/courses/{courseId}/done", courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("경로 변수로 받은 courseId가 유효하지 않으면, http status 400 반환한다.")
        void failEntityNotFound() throws Exception {
            // given
            Long invalidCourseId = 100L;

            Long userId = 1L;
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
                    patch("/courses/{courseId}/done", invalidCourseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("코스를 등록한 유저와 상태 변경 요청한 유저의 식별자가 다르면, http status 403 반환한다.")
        void failNoAuthorities() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

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
                    patch("/courses/{courseId}/done", courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isForbidden());
        }
    }
}