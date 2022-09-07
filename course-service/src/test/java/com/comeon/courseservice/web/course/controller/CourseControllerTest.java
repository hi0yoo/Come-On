package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.config.argresolver.JwtArgumentResolver;
import com.comeon.courseservice.docs.config.RestDocsSupport;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.feign.userservice.UserServiceFeignClient;
import com.comeon.courseservice.web.feign.userservice.response.ListResponse;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Import({S3MockConfig.class})
class CourseControllerTest extends RestDocsSupport {

    @Value("${s3.folder-name.course}")
    String dirName;

    @Autowired
    EntityManager em;

    @Autowired
    FileManager fileManager;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CoursePlaceRepository coursePlaceRepository;

    @Autowired
    CourseLikeRepository courseLikeRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtArgumentResolver jwtArgumentResolver;

    @Autowired
    CourseController courseController;

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @MockBean
    UserServiceFeignClient userServiceFeignClient;

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

    void initCourseAndPlaces() throws IOException {
        Long userId = 1L;
        String title = "courseTitle";
        String description = "courseDescription";

        File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.png"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "imgFile",
                "test-img.png",
                ContentType.IMAGE_JPEG.getMimeType(),
                new FileInputStream(imgFile)
        );
        UploadedFileInfo uploadedFileInfo = fileManager.upload(mockMultipartFile, dirName);
        CourseImage courseImage = CourseImage.builder()
                .originalName(uploadedFileInfo.getOriginalFileName())
                .storedName(uploadedFileInfo.getStoredFileName())
                .build();

        Course courseToSave = Course.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .courseImage(courseImage)
                .build();

        int count = 5;
        String placeName = "placeName";
        String placeDescription = "placeDescription";
        Double placeLat = 12.34;
        Double placeLng = 23.45;
        List<CoursePlace> coursePlaceList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            coursePlaceList.add(
                    CoursePlace.builder()
                            .course(courseToSave)
                            .name(placeName + i)
                            .description(placeDescription + i)
                            .lat(placeLat + i)
                            .lng(placeLng + i)
                            .order(i)
                            .mapPlaceId((long) i)
                            .build()
            );
        }
        course = courseRepository.save(courseToSave);
    }

    void initCourseLikes() {
        for (int i = 1; i <= 100; i++) {
            CourseLike courseLike = CourseLike.builder()
                    .userId((long) i)
                    .course(course)
                    .build();
            courseLikeRepository.save(courseLike);
        }
    }

    /*
        - 작성된 코스 개수 = 45개
        - 코스 작성자 id = 1, 2, 3
        - 코스당 코스 장소 개수 = 5개
        - 코스 식별값 % 5 == 0 => 좋아요 5개
        - 코스 식별값 % 7 == 0 => 좋아요 7개
        - 코스 식별값 % 9 == 0 => 좋아요 9개
        - 코스 식별값 % 13 == 0 => 좋아요 13개
        - 나머지 => 좋아요 3개
         */
    void initCourseListData() {
        Long coursePlaceId = 1L;
        for (int i = 1; i <= 15; i++) {
            for (int uid = 1; uid <= 3; uid++) {
                Course course = Course.builder()
                        .userId((long) uid)
                        .title("title" + uid + i)
                        .description("description" + uid + i)
                        .courseImage(
                                CourseImage.builder()
                                        .originalName("originalName" + uid + i)
                                        .storedName("storedName" + uid + i)
                                        .build()
                        )
                        .build();

                for (; coursePlaceId % 6 != 0; coursePlaceId++) {
                    CoursePlace.builder()
                            .course(course)
                            .name("name" + coursePlaceId)
                            .description("description" + coursePlaceId)
                            .lat(nextDouble() * (38 - 36 + 1) + 36)
                            .lng(nextDouble() * (128 - 126 + 1) + 126)
                            .order((int) (coursePlaceId % 6))
                            .mapPlaceId((long) coursePlaceId)
                            .build();
                }

                course.completeWriting();
                courseRepository.save(course);
                coursePlaceId++;

                if (i % 5 == 0) {
                    for (int likeUserId = 1; likeUserId <= 5; likeUserId++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) likeUserId)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else if (i % 7 == 0) {
                    for (int likeUserId = 2; likeUserId <= 8; likeUserId++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) likeUserId)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else if (i % 9 == 0) {
                    for (int likeUserId = 3; likeUserId <= 11; likeUserId++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) likeUserId)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else if (i % 13 == 0) {
                    for (int likeUserId = 4; likeUserId <= 16; likeUserId++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) likeUserId)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else {
                    for (int likeUserId = 5; likeUserId <= 7; likeUserId++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) likeUserId)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                }
            }
        }
    }

    String mockUserNickname = "userNickname";
    String mockUserProfileImgUrl = "userProfileImgUrl";
    String mockUserStatus = "ACTIVATE";

    private void setUserFeignOfGetUserDetails(Long userId) {
        given(userServiceFeignClient.getUserDetails(userId))
                .willReturn(ApiResponse.createSuccess(
                        new UserDetailsResponse(
                                userId,
                                mockUserNickname,
                                mockUserProfileImgUrl,
                                mockUserStatus)
                ));
    }

    private void setUserFeignOfGetUserList(List<Long> userIds) {
        List<UserInfo> users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(new UserInfo(
                            (long) i,
                            "usernickname" + i,
                            "profileImgUrl" + i,
                            "ACTIVATE"
                    )
            );
        }

        List<UserDetailsResponse> responseList = users.stream()
                .filter(userInfo -> userIds.contains(userInfo.getUserId()))
                .map(userInfo -> new UserDetailsResponse(
                        userInfo.getUserId(),
                        userInfo.getNickname(),
                        userInfo.getProfileImgUrl(),
                        userInfo.getStatus()
                ))
                .collect(Collectors.toList());

        given(userServiceFeignClient.userList(any()))
                .willReturn(ApiResponse.createSuccess(
                        ListResponse.toListResponse(responseList)
                ));
    }

    @Nested
    @DisplayName("코스 저장")
    class courseSave {

        @Test
        @DisplayName("[docs] 요청 데이터 검증에 성공하면, 코스를 저장하고, 해당 코스 식별자를 응답으로 반환한다.")
        void success() throws Exception {
            // given
            String title = "courseTitle";
            String description = "courseDescription";

            File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img.png"));
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imgFile",
                    "test-img.png",
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
                            requestParts(
                                    attributes(key("title").value("요청 파트")),
                                    partWithName("imgFile").description("등록할 이미지 파일")
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("title").description("코스의 제목"),
                                    parameterWithName("description").description("코스의 설명")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 요청 데이터 검증에 실패하면, http status 400 반환한다.")
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
                                    fieldWithPath("message.imgFile").ignored(),
                                    fieldWithPath("message.description").ignored(),
                                    fieldWithPath("message.title").ignored()
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("코스 단건 조회")
    class courseDetails {

        @Test
        @DisplayName("[docs] 작성 완료된 코스의 식별값으로 조회하면, 코스 데이터 조회에 성공하고 http status 200 반환한다.")
        void success() throws Exception {
            // given
            initCourseAndPlaces();
            course.completeWriting(); // 코스 작성 완료 처리
            initCourseLikes(); // 코스 좋아요 추가

            Long courseId = course.getId();

            setUserFeignOfGetUserDetails(course.getUserId());

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
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.title").value(course.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(course.getDescription()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.likeCount").value(course.getLikeCount()))
                    .andExpect(jsonPath("$.data.userLikeId").exists())
                    .andExpect(jsonPath("$.data.lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.writer").exists())
                    .andExpect(jsonPath("$.data.writer.userId").value(course.getUserId()))
                    .andExpect(jsonPath("$.data.writer.nickname").value(mockUserNickname))
                    .andExpect(jsonPath("$.data.coursePlaces").isNotEmpty());


            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("조회할 코스의 식별값")
                            ),
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("코스의 설명"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLikeId").type(JsonFieldType.NUMBER).description("현재 사용자가 등록한 좋아요 식별값").optional(),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),
                                    fieldWithPath("coursePlaces").type(JsonFieldType.ARRAY).description("코스에 등록된 장소 정보 목록"),
                                    fieldWithPath("coursePlaces[].coursePlaceId").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("coursePlaces[].name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("coursePlaces[].description").type(JsonFieldType.STRING).description("장소 설명"),
                                    fieldWithPath("coursePlaces[].lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("coursePlaces[].lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("coursePlaces[].order").type(JsonFieldType.NUMBER).description("장소의 순서값"),
                                    fieldWithPath("coursePlaces[].mapPlaceId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 작성자가 아닌 사용자가 작성 완료되지 않은 코스를 조회하면, http status 403 반환")
        void failNotCompleteCourseId() throws Exception {
            // given
            initCourse();

            Long courseId = course.getId();

            setUserFeignOfGetUserDetails(course.getUserId());

            // when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    get(path, courseId)
            );

            // then
            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.code").exists())
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
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 작성 완료되지 않은 코스라도, 해당 코스 작성자는 조회할 수 있다. http status 200 반환")
        void successWhenNotComplete() throws Exception {
            // given
            initCourse();

            Long courseId = course.getId();

            Long userId = course.getUserId();
            setUserFeignOfGetUserDetails(userId);

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
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.title").value(course.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(course.getDescription()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.writer").exists())
                    .andExpect(jsonPath("$.data.writer.userId").value(course.getUserId()))
                    .andExpect(jsonPath("$.data.writer.nickname").value(mockUserNickname))
                    .andExpect(jsonPath("$.data.coursePlaces").isEmpty());


            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).optional().description("로그인 및 재발급을 통해 발급받은, 유효한 AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("코스의 설명"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),
                                    fieldWithPath("coursePlaces").type(JsonFieldType.ARRAY).description("코스에 등록된 장소 정보 목록")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 존재하지 않는 코스의 식별값으로 조회하면, http status 400 반환")
        void failInvalidCourseId() throws Exception {
            // given
            Long courseId = 100L;

            setUserFeignOfGetUserDetails(100L);

            // when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    get(path, courseId)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
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
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("코스 리스트 조회")
    class courseList {

        @Test
        @DisplayName("[docs] 로그인한 경우 - 조회 성공")
        void success() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            List<Long> userIdList = List.of(1L, 2L, 3L);
            setUserFeignOfGetUserList(userIdList);

            Long userId = 3L;
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
                    get("/courses")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
                            .param("title", "")
                            .param("lat", String.valueOf(37.558685))
                            .param("lng", String.valueOf(126.967178))
            );

            // then
            perform.andExpect(status().isOk());
            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken").optional()
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional(),
                                    parameterWithName("title").description("코스 제목 검색어").optional(),
                                    parameterWithName("lat").description("사용자의 위도값").optional(),
                                    parameterWithName("lng").description("사용자의 경도값").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),
                                    fieldWithPath("firstPlace").type(JsonFieldType.OBJECT).description("코스에 등록된 첫번째 장소 목록"),
                                    fieldWithPath("firstPlace.coursePlaceId").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("firstPlace.lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("firstPlace.lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("firstPlace.distance").type(JsonFieldType.NUMBER).description("유저 위치와 해당 장소와의 거리")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 로그인 하지 않은 경우 - 조회 성공")
        void successUnAuthorized() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            List<Long> userIdList = List.of(1L, 2L, 3L);
            setUserFeignOfGetUserList(userIdList);

            // when
            ResultActions perform = mockMvc.perform(
                    get("/courses")
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
                            .param("title", "")
                            .param("lat", String.valueOf(37.558685))
                            .param("lng", String.valueOf(126.967178))
            );

            // then
            perform.andExpect(status().isOk());
            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken").optional()
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional(),
                                    parameterWithName("title").description("코스 제목 검색어").optional(),
                                    parameterWithName("lat").description("사용자의 위도값").optional(),
                                    parameterWithName("lng").description("사용자의 경도값").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),
                                    fieldWithPath("firstPlace").type(JsonFieldType.OBJECT).description("코스에 등록된 첫번째 장소 목록"),
                                    fieldWithPath("firstPlace.coursePlaceId").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("firstPlace.lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("firstPlace.lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("firstPlace.distance").type(JsonFieldType.NUMBER).description("유저 위치와 해당 장소와의 거리")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("현재 유저가 등록한 코스 리스트 조회")
    class myCourseList {

        @Test
        @DisplayName("[docs] 조회 성공")
        void success() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            Long userId = 2L;
            setUserFeignOfGetUserDetails(userId);

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
                    get("/courses/my")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
            );

            // then
            perform.andExpect(status().isOk());
            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());

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
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 로그인하지 않은 경우 - http status 401 오류 발생")
        void failUnAuthorized() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            Long userId = 2L;
            setUserFeignOfGetUserDetails(userId);

            // when
            ResultActions perform = mockMvc.perform(
                    get("/courses/my")
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
            );

            // then
            perform.andExpect(status().isUnauthorized());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("예외 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("현재 유저가 좋아요한 코스 리스트 조회")
    class courseLikeList {

        @Test
        @DisplayName("[docs] 조회 성공")
        void success() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            List<Long> userIdList = List.of(1L, 2L, 3L);
            setUserFeignOfGetUserList(userIdList);

            Long userId = 2L;
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
                    get("/courses/like")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
            );

            // then
            perform.andExpect(status().isOk());
            log.info("result : {}", perform.andReturn().getResponse().getContentAsString());

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
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),
                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.userId").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 로그인하지 않은 경우 - http status 401 오류 발생")
        void failUnAuthorized() throws Exception {
            // given
            initCourseListData();
            em.flush();
            em.clear();

            List<Long> userIdList = List.of(1L, 2L, 3L);
            setUserFeignOfGetUserList(userIdList);

            // when
            ResultActions perform = mockMvc.perform(
                    get("/courses/my")
                            .param("size", String.valueOf(10))
                            .param("page", String.valueOf(0))
            );

            // then
            perform.andExpect(status().isUnauthorized());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("예외 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("예외 메시지")
                            )
                    )
            );
        }
    }


    // customDtoClass
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class UserInfo {
        private Long userId;
        private String nickname;
        private String profileImgUrl;
        private String status;
    }
}