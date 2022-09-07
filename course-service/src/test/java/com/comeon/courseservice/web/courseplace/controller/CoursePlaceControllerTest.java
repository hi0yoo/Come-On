package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.JwtArgumentResolver;
import com.comeon.courseservice.docs.config.RestDocsSupport;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.web.courseplace.request.CoursePlaceSaveRequest;
import com.comeon.courseservice.web.courseplace.request.CoursePlacesBatchSaveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
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

    @Value("${jwt.secret}")
    String jwtSecretKey;

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
            Long placeId = 1L;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(name, description, lat, lng, placeId);

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
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
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
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("등록 대상 코스의 식별값")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("장소의 설명"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("placeId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값")
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
            Long courseId = course.getId();

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
                    post("/courses/{courseId}/course-places", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());
            // then
            perform.andExpect(status().isBadRequest())
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
            Long placeId = 1L;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(name, description, lat, lng, placeId);

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
                    post("/courses/{courseId}/course-places", courseId)
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
            Long placeId = 1L;

            CoursePlaceSaveRequest request = new CoursePlaceSaveRequest(name, description, lat, lng, placeId);

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
                    post("/courses/{courseId}/course-places", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("코스 장소 리스트 등록")
    class coursePlaceSaveBatch {
        @Test
        @DisplayName("[docs] 요청 데이터 검증에 성공하면, 해당 코스에 장소 리스트를 저장한다.")
        void success() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

            int count = 5;
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlacesBatchSaveRequest.CoursePlaceInfo> coursePlaceInfoList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                coursePlaceInfoList.add(
                        CoursePlacesBatchSaveRequest.CoursePlaceInfo.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .placeId((long) i)
                                .build()
                );
            }

            CoursePlacesBatchSaveRequest request = new CoursePlacesBatchSaveRequest(coursePlaceInfoList);

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
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").exists());

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
                                    parameterWithName("courseId").description("등록 대상 코스의 식별값")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    fieldWithPath("coursePlaces").type(JsonFieldType.ARRAY).description("대상 코스에 등록할 장소 리스트"),
                                    fieldWithPath("coursePlaces[].name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("coursePlaces[].description").type(JsonFieldType.STRING).description("장소의 설명"),
                                    fieldWithPath("coursePlaces[].lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("coursePlaces[].lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("coursePlaces[].order").type(JsonFieldType.NUMBER).description("장소의 순서"),
                                    fieldWithPath("coursePlaces[].placeId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("장소 리스트 저장 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 장소 리스트에서 특정 장소의 특정 필수값이 빠진 경우, 요청 데이터 검증에 실패하고 http status 400 반환, 응답에는 검증 오류 필드가 담긴다.")
        void failSpecificField() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

            int count = 5;
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlacesBatchSaveRequest.CoursePlaceInfo> coursePlaceInfoList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                coursePlaceInfoList.add(
                        CoursePlacesBatchSaveRequest.CoursePlaceInfo.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                // i가 홀수이면 lat 정보를 넣지 않는다. -> 검증 실패
                                .lat(i % 2 == 0 ? placeLat + i : null)
                                .lng(placeLng + i)
                                .order(i % 2 != 0 ? i : null)
                                .build()
                );
            }

            CoursePlacesBatchSaveRequest request = new CoursePlacesBatchSaveRequest(coursePlaceInfoList);

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
                    post("/courses/{courseId}/course-places/batch", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());
            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint())
                    )
            );
        }

        @Test
        @DisplayName("[docs] 장소 리스트가 없는 경우, 요청 데이터 검증에 실패하고 http status 400 반환, 응답에는 검증 오류 필드가 담긴다.")
        void failAllField() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

            CoursePlacesBatchSaveRequest request = new CoursePlacesBatchSaveRequest(null);

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
                    post("/courses/{courseId}/course-places/batch", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());
            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message").exists());

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
                                    fieldWithPath("message.coursePlaces").ignored()
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에는 성공했으나, 코스를 등록한 유저가 아니면, http status 403 반환한다.")
        void failInvalidUser() throws Exception {
            // given
            initCourse();
            Long courseId = course.getId();

            int count = 5;
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlacesBatchSaveRequest.CoursePlaceInfo> coursePlaceInfoList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                coursePlaceInfoList.add(
                        CoursePlacesBatchSaveRequest.CoursePlaceInfo.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .build()
                );
            }

            CoursePlacesBatchSaveRequest request = new CoursePlacesBatchSaveRequest(coursePlaceInfoList);

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
                    post("/courses/{courseId}/course-places/batch", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("요청 데이터 검증에는 성공했으나, 저장되지 않은 코스의 식별자가 넘어오면, http status 400 반환한다.")
        void failInvalidCourse() throws Exception {
            // given
            initCourse();
            Long courseId = 100L;
            int count = 5;
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlacesBatchSaveRequest.CoursePlaceInfo> coursePlaceInfoList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                coursePlaceInfoList.add(
                        CoursePlacesBatchSaveRequest.CoursePlaceInfo.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .build()
                );
            }

            CoursePlacesBatchSaveRequest request = new CoursePlacesBatchSaveRequest(coursePlaceInfoList);

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
                    post("/courses/{courseId}/course-places/batch", courseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest());
        }
    }
}