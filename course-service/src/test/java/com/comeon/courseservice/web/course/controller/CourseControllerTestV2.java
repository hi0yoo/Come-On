package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.service.CourseService;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.service.CourseLikeService;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureRestDocs
@ActiveProfiles("test")
@WebMvcTest(CourseController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CourseControllerTestV2 {

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @MockBean
    FileManager fileManager;

    @MockBean
    CourseQueryService courseQueryService;

    @MockBean
    CourseLikeService courseLikeService;

    @MockBean
    CourseService courseService;

    @Autowired
    MockMvc mockMvc;

    Course course;

    void initCourseAndPlaces() {
        course = Course.builder()
                .userId(1L)
                .title("courseTitle")
                .description("courseDescription")
                .courseImage(
                        CourseImage.builder()
                                .originalName("originalFileName")
                                .storedName("storedFileName")
                                .build()
                )
                .build();
        ReflectionTestUtils.setField(course, "id", 1L);

        int count = 5;
        String placeName = "placeName";
        String placeDescription = "placeDescription";
        Double placeLat = 12.34;
        Double placeLng = 23.45;
        List<CoursePlace> coursePlaceList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name(placeName + i)
                    .description(placeDescription + i)
                    .lat(placeLat + i)
                    .lng(placeLng + i)
                    .order(i)
                    .build();
            ReflectionTestUtils.setField(coursePlace, "id", (long) i);
            coursePlaceList.add(coursePlace);
        }
    }

    List<CourseLike> courseLikes = new ArrayList<>();

    void initCourseAndPlacesAndLike() {
        initCourseAndPlaces();

        for (int i = 1; i <= 50; i++) {
            CourseLike courseLike = CourseLike.builder()
                    .course(course)
                    .userId((long) i)
                    .build();
            ReflectionTestUtils.setField(courseLike, "id", (long) i);
            courseLikes.add(courseLike);
        }
    }

    @AfterEach
    void clear() {
        course = null;
        courseLikes.clear();
    }

    @Nested
    @DisplayName("코스 좋아요 등록 및 취소")
    class courseLikeModify {

        private CourseLike createCourseLike() {
            CourseLike courseLike = CourseLike.builder()
                    .course(course)
                    .build();
            ReflectionTestUtils.setField(courseLike, "id", 1L);
            courseLikes.add(courseLike);
            return courseLike;
        }

        private void setMocks(Long courseId, Long userId) {
            given(courseLikeService.modifyCourseLike(courseId, userId))
                    .will(invocation -> {
                        if (Objects.isNull(userId)) {
                            throw new CustomException(ErrorCode.INVALID_AUTHORIZATION_HEADER);
                        }
                        if (course == null || !courseId.equals(course.getId())) {
                            throw new EntityNotFoundException();
                        }
                        return courseLikes.stream()
                                .filter(
                                        courseLike -> courseLike.getCourse().getId().equals(courseId) && courseLike.getUserId().equals(userId)
                                )
                                .findFirst()
                                .orElse(createCourseLike())
                                .getId();
                    });
        }

        @Test
        @DisplayName("[docs] 현재 유저가 코스에 좋아요를 등록하지 않았을 경우, 좋아요를 등록하고 CREATED를 응답한다.")
        void successCreated() throws Exception {
            // given
            initCourseAndPlaces();
            Long courseId = course.getId();
            Long userId = 1L;

            setMocks(courseId, userId);

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
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.likeResult").exists());

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
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("좋아요를 등록할 대상 코스 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("likeResult").type(JsonFieldType.STRING).description("좋아요 상태 변경 결과")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 현재 유저가 코스에 좋아요를 등록했었던 경우, 좋아요를 삭제하고 DELETED를 응답한다.")
        void successDeleted() throws Exception {
            // given
            initCourseAndPlacesAndLike();
            Long courseId = course.getId();
            Long userId = 1L;

            setMocks(courseId, userId);

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
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.likeResult").exists());

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
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("좋아요를 등록할 대상 코스 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("likeResult").type(JsonFieldType.STRING).description("좋아요 상태 변경 결과")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 경로 파라미터로 넘어온 코스 식별값과 일치하는 코스가 없으면, http status 400 오류를 반환한다.")
        void invalidCourseId() throws Exception {
            // given
            Long courseId = 100L;
            Long userId = 1L;

            setMocks(courseId, userId);

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
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isBadRequest());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 로그인 하지 않은 유저가 요청을 보내면, http status 401 오류를 반환한다.")
        void unAuthorized() throws Exception {
            // given
            initCourseAndPlaces();
            Long courseId = course.getId();

            setMocks(courseId, null);

            // when
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
            );

            // then
            perform.andExpect(status().isUnauthorized());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("API 오류 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }
}
