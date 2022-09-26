package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.docs.utils.RestDocsUtil;
import com.comeon.courseservice.domain.common.BaseTimeEntity;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.utils.DistanceUtils;
import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.service.CourseService;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.service.CourseLikeService;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.AbstractControllerTest;
import com.comeon.courseservice.web.common.aop.ValidationAspect;
import com.comeon.courseservice.web.common.response.SliceResponse;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.course.query.repository.cond.CourseCondition;
import com.comeon.courseservice.web.course.query.repository.cond.MyCourseCondition;
import com.comeon.courseservice.web.course.query.repository.dto.CourseListData;
import com.comeon.courseservice.web.course.query.repository.dto.MyPageCourseListData;
import com.comeon.courseservice.web.course.request.CourseListRequestValidator;
import com.comeon.courseservice.web.course.response.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Import({
        AopAutoConfiguration.class,
        ValidationAspect.class,
        CourseListRequestValidator.class
})
@WebMvcTest(CourseController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CourseControllerTest extends AbstractControllerTest {

    @MockBean
    CourseQueryService courseQueryService;

    @MockBean
    CourseLikeService courseLikeService;

    @MockBean
    CourseService courseService;

    @Nested
    @DisplayName("코스 저장")
    class courseSave {

        @Test
        @DisplayName("[docs] 요청 데이터 검증에 성공하면, 코스를 저장하고, 해당 코스 식별자를 응답으로 반환한다.")
        void success() throws Exception {
            //given
            String title = "courseTitle";
            String description = "courseDescription";
            Long userId = 1L;

            String accessToken = generateUserAccessToken(userId);

            // mocking
            MockMultipartFile mockMultipartFile = getMockMultipartFile("test-img.png");
            CourseDto courseDto = new CourseDto(userId, title, description, any());
            Long courseId = 1L;
            given(courseService.saveCourse(courseDto))
                    .willReturn(courseId);
            given(courseQueryService.getCourseStatus(courseId))
                    .willReturn(CourseStatus.WRITING);

            //when
            ResultActions perform = mockMvc.perform(
                    multipart("/courses")
                            .file(mockMultipartFile)
                            .param("title", title)
                            .param("description", description)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(1L));

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS))
//                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description("저장된 코스의 작성 상태. " +
//                                                    "새로 생성된 코스는 연관된 장소 데이터가 없기 때문에 항상 WRITING(작성중) 상태.")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면 http status 400 반환하고, 검증 실패한 필드가 메시지에 담긴다.")
        void validationFail() throws Exception {
            Long userId = 1L;
            String accessToken = generateUserAccessToken(userId);

            // when
            ResultActions perform = mockMvc.perform(
                    multipart("/courses")
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.message.imgFile").exists())
                    .andExpect(jsonPath("$.data.message.description").exists())
                    .andExpect(jsonPath("$.data.message.title").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("오류 메시지"),
                                    fieldWithPath("message.imgFile").ignored(),
                                    fieldWithPath("message.description").ignored(),
                                    fieldWithPath("message.title").ignored()
                            )
                    )
            );
        }

        // TODO 로그인 되지 않은 사용자는 401 Error
    }

    @Nested
    @DisplayName("코스 단건 조회")
    class courseDetails {

        @Test
        @DisplayName("작성 완료된 코스의 식별값으로 조회하면, 코스 데이터 조회에 성공하고 http status 200 반환한다.")
        void success() throws Exception {
            // given
            initData();
            Course course = getCourseList().stream()
                    .findFirst()
                    .orElseThrow();

            Long courseId = course.getId();
            Long currentUserId = 1L;

            // mocking
            String mockWriterNickname = "userNickname";
            CourseDetailResponse courseDetailResponse = new CourseDetailResponse(
                    course,
                    new UserDetailInfo(course.getUserId(), mockWriterNickname),
                    fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName),
                    true
            );

            given(courseQueryService.getCourseDetails(courseId, currentUserId))
                    .willReturn(courseDetailResponse);

            // when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.title").value(course.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(course.getDescription()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.likeCount").value(course.getLikeCount()))
                    .andExpect(jsonPath("$.data.userLiked").exists())
                    .andExpect(jsonPath("$.data.lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.writer").exists())
                    .andExpect(jsonPath("$.data.writer.id").value(course.getUserId()))
                    .andExpect(jsonPath("$.data.writer.nickname").value(mockWriterNickname))
                    .andExpect(jsonPath("$.data.coursePlaces").isNotEmpty())
                    .andExpect(jsonPath("$.data.courseStatus").value(CourseStatus.COMPLETE.name()));


            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("해당 코스에 대한 현재 사용자의 좋아요 여부"),

                                    fieldWithPath("coursePlaces").type(JsonFieldType.ARRAY).description("코스에 등록된 장소 정보 목록"),
                                    fieldWithPath("coursePlaces[].id").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("coursePlaces[].name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("coursePlaces[].description").type(JsonFieldType.STRING).description("장소 설명"),
                                    fieldWithPath("coursePlaces[].lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("coursePlaces[].lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("coursePlaces[].order").type(JsonFieldType.NUMBER).description("장소의 순서값"),
                                    fieldWithPath("coursePlaces[].apiId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값"),
                                    fieldWithPath("coursePlaces[].category").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.PLACE_CATEGORY))
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 코스 식별값이 들어오면 Http status 400 반환한다.")
        void notExistCourse() throws Exception {
            // given
            Long courseId = 100L;
            Long currentUserId = 1L;

            // mocking
            given(courseQueryService.getCourseDetails(courseId, currentUserId))
                    .willThrow(new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId));

            // when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("작성 완료되지 않은 코스는 작성자가 아닌 사용자가 조회할 수 없다.")
        void canNotOpenCourseWhichDoesNotComplete() throws Exception {
            //given
            Long courseId = 10L;
            Long currentUserId = 1L;

            // mocking
            given(courseQueryService.getCourseDetails(courseId, currentUserId))
                    .willThrow(new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.CAN_NOT_ACCESS_RESOURCE));

            // when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("해당 코스의 작성자는 작성 완료되지 않아도 조회할 수 있다.")
        void writerCanOpenCourseWhichDoesNotComplete() throws Exception {
            //given
            Long currentUserId = 1L;
            Course course = setCourses(currentUserId, 1)
                    .stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            // mocking
            String mockWriterNickname = "userNickname";
            CourseDetailResponse courseDetailResponse = new CourseDetailResponse(
                    course,
                    new UserDetailInfo(course.getUserId(), mockWriterNickname),
                    fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName),
                    true
            );

            given(courseQueryService.getCourseDetails(courseId, currentUserId))
                    .willReturn(courseDetailResponse);

            // when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.title").value(course.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(course.getDescription()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.likeCount").value(course.getLikeCount()))
                    .andExpect(jsonPath("$.data.userLiked").exists())
                    .andExpect(jsonPath("$.data.lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.writer").exists())
                    .andExpect(jsonPath("$.data.writer.id").value(course.getUserId()))
                    .andExpect(jsonPath("$.data.writer.nickname").value(mockWriterNickname))
                    .andExpect(jsonPath("$.data.coursePlaces").isEmpty())
                    .andExpect(jsonPath("$.data.courseStatus").value(CourseStatus.WRITING.name()));

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("해당 코스에 대한 현재 사용자의 좋아요 여부"),

                                    subsectionWithPath("coursePlaces").type(JsonFieldType.ARRAY).description("코스에 등록된 장소 정보 목록. 코스 상태가 `작성중` 이라면, 비어있는 배열을 반환.")
                            )
                    )
            );
        }

        @Test
        @DisplayName("로그인하지 않은 유저도 코스를 조회할 수 있다.")
        void canOpenCourseWhoDoesNotLogin() throws Exception {
            // given
            initData();
            Course course = getCourseList().stream()
                    .findFirst()
                    .orElseThrow();

            Long courseId = course.getId();

            // mocking
            String mockWriterNickname = "userNickname";
            CourseDetailResponse courseDetailResponse = new CourseDetailResponse(
                    course,
                    new UserDetailInfo(course.getUserId(), mockWriterNickname),
                    fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName),
                    false
            );

            given(courseQueryService.getCourseDetails(courseId, null))
                    .willReturn(courseDetailResponse);

            // when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.title").value(course.getTitle()))
                    .andExpect(jsonPath("$.data.description").value(course.getDescription()))
                    .andExpect(jsonPath("$.data.imageUrl").exists())
                    .andExpect(jsonPath("$.data.likeCount").value(course.getLikeCount()))
                    .andExpect(jsonPath("$.data.userLiked").exists())
                    .andExpect(jsonPath("$.data.lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.writer").exists())
                    .andExpect(jsonPath("$.data.writer.id").value(course.getUserId()))
                    .andExpect(jsonPath("$.data.writer.nickname").value(mockWriterNickname))
                    .andExpect(jsonPath("$.data.coursePlaces").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("코스 리스트 조회")
    class courseList {

        private double getDistance(CoursePlace coursePlace) {
            return DistanceUtils.distance(37.555945, 126.972331, coursePlace.getLat(), coursePlace.getLng());
        }

        private double getDistance(Double userLat, Double userLng, CoursePlace coursePlace) {
            return DistanceUtils.distance(userLat, userLng, coursePlace.getLat(), coursePlace.getLng());
        }

        @Test
        @DisplayName("위도와 경도를 모두 입력하지 않아도 코스 리스트 조회에 성공한다.")
        void success() throws Exception {
            //given
            initData();
            Long currentUserId = 1L;

            int pageSize = 10;

            Comparator<CourseListData> placeComparator = Comparator.comparing(CourseListData::getDistance);
            Comparator<CourseListData> lastModifyDateComparator = Comparator.comparing(o -> o.getCourse().getLastModifiedDate(), Comparator.reverseOrder());
            Comparator<CourseListData> likeCountComparator = Comparator.comparing(o -> o.getCourse().getLikeCount(), Comparator.reverseOrder());

            List<CourseListData> dataList = getCourseList().stream()
                    .filter(course -> !course.getCoursePlaces().isEmpty())
                    .filter(Course::isWritingComplete)
                    .map(course -> {
                        CoursePlace place = course.getCoursePlaces().stream()
                                .filter(coursePlace -> coursePlace.getOrder().equals(1))
                                .findFirst()
                                .orElseThrow();
                        Long courseLikeId = getCourseLikeList().stream()
                                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(currentUserId))
                                .findFirst()
                                .map(CourseLike::getId)
                                .orElse(null);
                        return new CourseListData(course, place, getDistance(place), courseLikeId);
                    })
                    .filter(courseListData -> courseListData.getDistance() <= 100)
                    .sorted(placeComparator.thenComparing(likeCountComparator).thenComparing(lastModifyDateComparator))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<CourseListResponse> courseListResponses = new ArrayList<>();
            for (CourseListData courseListData : dataList) {
                Course course = courseListData.getCourse();
                CourseListResponse courseListResponse = CourseListResponse.builder()
                        .course(course)
                        .imageUrl(fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName))
                        .coursePlace(courseListData.getCoursePlace())
                        .firstPlaceDistance(courseListData.getDistance())
                        .writer(new UserDetailInfo(course.getUserId(), "writerNickname" + course.getUserId()))
                        .userLiked(Objects.nonNull(courseListData.getUserLikeId()))
                        .build();
                courseListResponses.add(courseListResponse);
            }

            SliceResponse<CourseListResponse> courseListResponseSliceResponse =
                    SliceResponse.toSliceResponse(new SliceImpl<>(courseListResponses, PageRequest.of(0, pageSize), true));

            // mocking
            given(courseQueryService.getCourseList(eq(currentUserId), any(CourseCondition.class), any(Pageable.class)))
                    .willReturn(courseListResponseSliceResponse);

            //when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            log.info(perform.andReturn().getResponse().getContentAsString());

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contents[*].courseId").exists())
                    .andExpect(jsonPath("$.data.contents[*].title").exists())
                    .andExpect(jsonPath("$.data.contents[*].imageUrl").exists())
                    .andExpect(jsonPath("$.data.contents[*].courseStatus").exists())
                    .andExpect(jsonPath("$.data.contents[*].lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.contents[*].likeCount").exists())
                    .andExpect(jsonPath("$.data.contents[*].userLiked").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.id").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.nickname").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.id").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.lat").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.lng").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.distance").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken").optional()
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional(),
                                    parameterWithName("title").description("코스 제목 검색어. 검색어가 코스의 제목에 포함되는 코스들만 조회.").optional(),
                                    parameterWithName("lat").description("사용자의 위도값. 위도, 경도 중 하나만 보내면 오류 발생.").optional(),
                                    parameterWithName("lng").description("사용자의 경도값. 위도, 경도 중 하나만 보내면 오류 발생.").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),

                                    fieldWithPath("firstPlace").type(JsonFieldType.OBJECT).description("코스에 등록된 첫번째 장소 목록"),
                                    fieldWithPath("firstPlace.id").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("firstPlace.lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("firstPlace.lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("firstPlace.distance").type(JsonFieldType.NUMBER).description("유저 위치와 해당 장소와의 거리. 단위는 `km`")
                            )
                    )
            );
        }

        @Test
        @DisplayName("위도와 경도, 제목 검색어를 모두 입력하면 제목에 검색어가 포함된 코스 리스트만 조회한다.")
        void successByAllParams() throws Exception {
            //given
            initData();

            Long currentUserId = 1L;
            String searchWords = "1";
            Double userLat = 37.0;
            Double userLng = 127.0;

            int pageNum = 0;
            int pageSize = 10;

            Comparator<CourseListData> placeComparator = Comparator.comparing(CourseListData::getDistance);
            Comparator<CourseListData> lastModifyDateComparator = Comparator.comparing(o -> o.getCourse().getLastModifiedDate(), Comparator.reverseOrder());
            Comparator<CourseListData> likeCountComparator = Comparator.comparing(o -> o.getCourse().getLikeCount(), Comparator.reverseOrder());

            List<CourseListData> dataList = getCourseList().stream()
                    .filter(course -> !course.getCoursePlaces().isEmpty())
                    .filter(Course::isWritingComplete)
                    .filter(course -> course.getTitle().toUpperCase().contains(searchWords.toUpperCase()))
                    .map(course -> {
                        CoursePlace place = course.getCoursePlaces().stream()
                                .filter(coursePlace -> coursePlace.getOrder().equals(1))
                                .findFirst()
                                .orElseThrow();
                        Long courseLikeId = getCourseLikeList().stream()
                                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(currentUserId))
                                .findFirst()
                                .map(CourseLike::getId)
                                .orElse(null);
                        return new CourseListData(course, place, getDistance(userLat, userLng, place), courseLikeId);
                    })
                    .filter(courseListData -> courseListData.getDistance() <= 100)
                    .sorted(placeComparator.thenComparing(likeCountComparator).thenComparing(lastModifyDateComparator))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<CourseListResponse> courseListResponses = new ArrayList<>();
            for (CourseListData courseListData : dataList) {
                Course course = courseListData.getCourse();
                CourseListResponse courseListResponse = CourseListResponse.builder()
                        .course(course)
                        .imageUrl(fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName))
                        .coursePlace(courseListData.getCoursePlace())
                        .firstPlaceDistance(courseListData.getDistance())
                        .writer(new UserDetailInfo(course.getUserId(), "writerNickname" + course.getUserId()))
                        .userLiked(Objects.nonNull(courseListData.getUserLikeId()))
                        .build();
                courseListResponses.add(courseListResponse);
            }

            SliceResponse<CourseListResponse> courseListResponseSliceResponse =
                    SliceResponse.toSliceResponse(new SliceImpl<>(courseListResponses, PageRequest.of(pageNum, pageSize), false));

            // mocking
            given(courseQueryService.getCourseList(eq(currentUserId), any(CourseCondition.class), any(Pageable.class)))
                    .willReturn(courseListResponseSliceResponse);

            //when
            String accessToken = generateUserAccessToken(currentUserId);
            String path = "/courses";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .param("page", String.valueOf(pageNum))
                            .param("title", searchWords)
                            .param("lat", String.valueOf(userLat))
                            .param("lng", String.valueOf(userLng))
            );

            //then
            log.info(perform.andReturn().getResponse().getContentAsString());

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contents[*].courseId").exists())
                    .andExpect(jsonPath("$.data.contents[*].title").exists())
                    .andExpect(jsonPath("$.data.contents[*].imageUrl").exists())
                    .andExpect(jsonPath("$.data.contents[*].courseStatus").exists())
                    .andExpect(jsonPath("$.data.contents[*].lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.contents[*].likeCount").exists())
                    .andExpect(jsonPath("$.data.contents[*].userLiked").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.id").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.nickname").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.id").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.lat").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.lng").exists())
                    .andExpect(jsonPath("$.data.contents[*].firstPlace.distance").exists());

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임"),

                                    fieldWithPath("firstPlace").type(JsonFieldType.OBJECT).description("코스에 등록된 첫번째 장소 목록"),
                                    fieldWithPath("firstPlace.id").type(JsonFieldType.NUMBER).description("장소의 식별값"),
                                    fieldWithPath("firstPlace.lat").type(JsonFieldType.NUMBER).description("장소의 위도값"),
                                    fieldWithPath("firstPlace.lng").type(JsonFieldType.NUMBER).description("장소의 경도값"),
                                    fieldWithPath("firstPlace.distance").type(JsonFieldType.NUMBER).description("유저 위치와 해당 장소와의 거리. 단위는 `km`")
                            )
                    )
            );
        }

        @Test
        @DisplayName("위도만 입력하고 경도를 입력하지 않으면 검증 오류가 발생하고 http status 400 반환한다.")
        void validationFailByNoLng() throws Exception {
            //given

            //when
            String path = "/courses";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .param("lat", String.valueOf(37.0))
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("위도는 입력하지 않고 경도만 입력하면 검증 오류가 발생하고 http status 400 반환한다.")
        void validationFailByNoLat() throws Exception {
            //given

            //when
            String path = "/courses";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .param("lng", String.valueOf(127.0))
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("예외 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("내가 등록한 코스 리스트 조회")
    class myCourseList {

        @Test
        @DisplayName("코스 상태 파라미터를 COMPLETE로 지정한 경우")
        void successCompleteCourses() throws Exception {
            //given
            int pageNum = 0;
            int pageSize = 10;
            Long currentUserId = 1L;

            initData(); // 작성 완료된 코스만 추가
            getCourseList().addAll(setCourses(currentUserId, 10)); // 작성 완료되지 않은 코스 추가
            CourseStatus courseStatus = CourseStatus.COMPLETE;

            List<MyPageCourseListData> listData = getCourseList().stream()
                    .filter(course -> course.getUserId().equals(currentUserId))
                    .filter(course -> course.getCourseStatus().equals(courseStatus)) // 코스 상태 일치하는 것 만 필터링
                    .map(course -> {
                        Long courseLikeId = getCourseLikeList().stream()
                                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(currentUserId))
                                .findFirst()
                                .map(CourseLike::getId)
                                .orElse(null);
                        return new MyPageCourseListData(course, courseLikeId);
                    })
                    .sorted(Comparator.comparing(myPageCourseListData -> myPageCourseListData.getCourse().getLastModifiedDate(), Comparator.reverseOrder()))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<MyPageCourseListResponse> myPageCourseListResponses = new ArrayList<>();
            for (MyPageCourseListData courseListData : listData) {
                Course course = courseListData.getCourse();
                MyPageCourseListResponse myPageCourseListResponse = MyPageCourseListResponse.builder()
                        .course(course)
                        .imageUrl(fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName))
                        .writer(new UserDetailInfo(course.getUserId(), "writerNickname" + course.getUserId()))
                        .userLiked(Objects.nonNull(courseListData.getUserLikeId()))
                        .build();
                myPageCourseListResponses.add(myPageCourseListResponse);
            }

            SliceResponse<MyPageCourseListResponse> courseListResponseSliceResponse =
                    SliceResponse.toSliceResponse(new SliceImpl<>(myPageCourseListResponses, PageRequest.of(pageNum, pageSize), true));

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getMyRegisteredCourseList(eq(currentUserId), refEq(new MyCourseCondition(courseStatus)), any(Pageable.class)))
                    .willReturn(courseListResponseSliceResponse);

            //when
            String path = "/courses/my";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .param("courseStatus", courseStatus.name())
                            .param("page", String.valueOf(pageNum))
                            .param("size", String.valueOf(pageSize))
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contents[*].courseId").exists())
                    .andExpect(jsonPath("$.data.contents[*].title").exists())
                    .andExpect(jsonPath("$.data.contents[*].imageUrl").exists())
                    .andExpect(jsonPath("$.data.contents[*].courseStatus").exists())
                    // COMPLETE가 아닌 courseStatus는 없다.
                    .andExpect(jsonPath("$.data.contents[?(@.courseStatus != '%s')].courseStatus", courseStatus.name()).doesNotExist())
                    .andExpect(jsonPath("$.data.contents[*].lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.contents[*].likeCount").exists())
                    .andExpect(jsonPath("$.data.contents[*].userLiked").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.id").exists())
                    .andExpect(jsonPath("$.data.contents[?(@.writer.id != %d)].writer.id", currentUserId).doesNotExist())
                    .andExpect(jsonPath("$.data.contents[*].writer.nickname").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("courseStatus").description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임")
                            )
                    )
            );
        }

        @Test
        @DisplayName("코스 상태 파라미터를 WRITING으로 지정한 경우")
        void successWritingCourses() throws Exception {
            //given
            int pageNum = 0;
            int pageSize = 10;
            Long currentUserId = 1L;

            initData(); // 작성 완료된 코스만 추가
            getCourseList().addAll(setCourses(currentUserId, 10)); // 작성 완료되지 않은 코스 추가
            CourseStatus courseStatus = CourseStatus.WRITING;

            List<MyPageCourseListData> listData = getCourseList().stream()
                    .filter(course -> course.getUserId().equals(currentUserId))
                    .filter(course -> course.getCourseStatus().equals(courseStatus)) // 코스 상태 일치하는 것 만 필터링
                    .map(course -> {
                        Long courseLikeId = getCourseLikeList().stream()
                                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(currentUserId))
                                .findFirst()
                                .map(CourseLike::getId)
                                .orElse(null);
                        return new MyPageCourseListData(course, courseLikeId);
                    })
                    .sorted(Comparator.comparing(myPageCourseListData -> myPageCourseListData.getCourse().getLastModifiedDate(), Comparator.reverseOrder()))
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<MyPageCourseListResponse> myPageCourseListResponses = new ArrayList<>();
            for (MyPageCourseListData courseListData : listData) {
                Course course = courseListData.getCourse();
                MyPageCourseListResponse myPageCourseListResponse = MyPageCourseListResponse.builder()
                        .course(course)
                        .imageUrl(fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName))
                        .writer(new UserDetailInfo(course.getUserId(), "writerNickname" + course.getUserId()))
                        .userLiked(Objects.nonNull(courseListData.getUserLikeId()))
                        .build();
                myPageCourseListResponses.add(myPageCourseListResponse);
            }

            SliceResponse<MyPageCourseListResponse> courseListResponseSliceResponse =
                    SliceResponse.toSliceResponse(new SliceImpl<>(myPageCourseListResponses, PageRequest.of(pageNum, pageSize), false));

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getMyRegisteredCourseList(eq(currentUserId), refEq(new MyCourseCondition(courseStatus)), any(Pageable.class)))
                    .willReturn(courseListResponseSliceResponse);

            //when
            String path = "/courses/my";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .param("courseStatus", courseStatus.name())
                            .param("page", String.valueOf(pageNum))
                            .param("size", String.valueOf(pageSize))
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contents[*].courseId").exists())
                    .andExpect(jsonPath("$.data.contents[*].title").exists())
                    .andExpect(jsonPath("$.data.contents[*].imageUrl").exists())
                    .andExpect(jsonPath("$.data.contents[*].courseStatus").exists())
                    // WRITING이 아닌 courseStatus는 없다.
                    .andExpect(jsonPath("$.data.contents[?(@.courseStatus != '%s')].courseStatus", courseStatus.name()).doesNotExist())
                    .andExpect(jsonPath("$.data.contents[*].lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.contents[*].likeCount").exists())
                    .andExpect(jsonPath("$.data.contents[*].userLiked").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.id").exists())
                    .andExpect(jsonPath("$.data.contents[?(@.writer.id != %d)].writer.id", currentUserId).doesNotExist())
                    .andExpect(jsonPath("$.data.contents[*].writer.nickname").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("courseStatus").description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("저장된 코스의 식별값"),
                                    fieldWithPath("title").type(JsonFieldType.STRING).description("코스의 제목 정보"),
                                    fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("코스의 이미지 URL"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임")
                            )
                    )
            );
        }

        @Test
        @DisplayName("코스 상태 파라미터를 지정하지 않으면 검증에 실패하고, http status 400 반환한다.")
        void noCourseStatusError() throws Exception {
            //given
            Long currentUserId = 1L;

            String accessToken = generateUserAccessToken(currentUserId);

            //when
            String path = "/courses/my";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 응답 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("코스 상태 파라미터를 잘 못 지정하면 검증 오류가 발생하여 http status 400 반환한다.")
        void validationError() throws Exception {
            //given
            Long currentUserId = 1L;

            String accessToken = generateUserAccessToken(currentUserId);

            //when
            String path = "/courses/my";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .param("courseStatus", "CONTINUE")
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("courseStatus").description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    parameterWithName("page").description("조회할 페이지 번호. 기본값 0").optional(),
                                    parameterWithName("size").description("페이지당 조회할 데이터 개수. 기본값 10").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("오류 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 응답 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("내가 좋아요한 코스 리스트 조회")
    class myCourseLikeList {

        @Test
        @DisplayName("로그인한 유저는 자신이 좋아요한 코스 리스트 조회에 성공한다.")
        void success() throws Exception {
            //given
            int pageNum = 0;
            int pageSize = 10;
            Long currentUserId = 1L;
            initData();
            List<MyPageCourseListData> listData = getCourseLikeList().stream()
                    .filter(courseLike -> courseLike.getUserId().equals(currentUserId))
                    .sorted(Comparator.comparing(BaseTimeEntity::getLastModifiedDate, Comparator.reverseOrder()))
                    .map(CourseLike::getCourse)
//                    .filter(course -> !course.getCoursePlaces().isEmpty())
                    .filter(Course::isWritingComplete)
                    .map(course -> {
                        Long courseLikeId = getCourseLikeList().stream()
                                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(currentUserId))
                                .findFirst()
                                .map(CourseLike::getId)
                                .orElse(null);
                        return new MyPageCourseListData(course, courseLikeId);
                    })
                    .limit(pageSize)
                    .collect(Collectors.toList());

            List<MyPageCourseListResponse> myPageCourseListResponses = new ArrayList<>();
            for (MyPageCourseListData courseListData : listData) {
                Course course = courseListData.getCourse();
                MyPageCourseListResponse myPageCourseListResponse = MyPageCourseListResponse.builder()
                        .course(course)
                        .imageUrl(fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName))
                        .writer(new UserDetailInfo(course.getUserId(), "writerNickname" + course.getUserId()))
                        .userLiked(Objects.nonNull(courseListData.getUserLikeId()))
                        .build();
                myPageCourseListResponses.add(myPageCourseListResponse);
            }

            SliceResponse<MyPageCourseListResponse> courseListResponseSliceResponse =
                    SliceResponse.toSliceResponse(new SliceImpl<>(myPageCourseListResponses, PageRequest.of(pageNum, pageSize), true));

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getMyLikedCourseList(eq(currentUserId), any(Pageable.class)))
                    .willReturn(courseListResponseSliceResponse);

            //when
            String path = "/courses/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.contents[*].courseId").exists())
                    .andExpect(jsonPath("$.data.contents[*].title").exists())
                    .andExpect(jsonPath("$.data.contents[*].imageUrl").exists())
                    .andExpect(jsonPath("$.data.contents[*].courseStatus").exists())
                    .andExpect(jsonPath("$.data.contents[*].lastModifiedDate").exists())
                    .andExpect(jsonPath("$.data.contents[*].likeCount").exists())
                    .andExpect(jsonPath("$.data.contents[*].userLiked").exists())
                    .andExpect(jsonPath("$.data.contents[?(@.userLiked == false)].userLiked").doesNotExist())
                    .andExpect(jsonPath("$.data.contents[*].writer").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.id").exists())
                    .andExpect(jsonPath("$.data.contents[*].writer.nickname").exists());

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("lastModifiedDate").type(JsonFieldType.STRING).description("해당 코스가 마지막으로 수정된 일자"),

                                    fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("해당 코스의 좋아요 수"),
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("현재 유저가 좋아요 했는지 여부"),

                                    fieldWithPath("writer").type(JsonFieldType.OBJECT).description("해당 코스 작성자"),
                                    fieldWithPath("writer.id").type(JsonFieldType.NUMBER).description("해당 코스 작성자 식별값"),
                                    fieldWithPath("writer.nickname").type(JsonFieldType.STRING).description("해당 코스 작성자 닉네임")
                            )
                    )
            );
        }

        // TODO 로그인하지 않으면 사용할 수 없다.
    }

    @Nested
    @DisplayName("코스 수정")
    class courseModify {

        @Test
        @DisplayName("요청 데이터 검증에 성공하고, 코스 수정에 성공하면, 성공 메시지를 응답으로 반환한다.")
        void success() throws Exception {
            //given
            Long currentUserId = 1L;
            Course course = setCourses(currentUserId, 1).stream()
                    .findFirst()
                    .orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 3);

            String title = "modifiedTitle";
            String description = "modifiedDescription";

            String accessToken = generateUserAccessToken(currentUserId);

            MockMultipartFile mockMultipartFile = getMockMultipartFile("test-img2.png");

            // mocking
            given(courseQueryService.getStoredFileName(courseId)).willReturn(course.getCourseImage().getStoredName());
            willDoNothing().given(courseService).modifyCourse(eq(courseId), any());

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.multipart(path, courseId)
                            .file(mockMultipartFile)
                            .param("title", title)
                            .param("description", description)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("수정할 코스 식별값")
                            ),
                            requestParts(
                                    attributes(key("title").value("요청 파트")),
                                    partWithName("imgFile").description("수정할 이미지 파일").optional()
                            ),
                            requestParameters(
                                    attributes(key("title").value("요청 파라미터")),
                                    parameterWithName("title").description("코스의 제목"),
                                    parameterWithName("description").description("코스의 설명")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("코스 수정 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터 검증에 실패하면 Http status 400 반환한다.")
        void validationFail() throws Exception {
            //given
            Long currentUserId = 1L;
            Course course = setCourses(currentUserId, 1).stream()
                    .findFirst()
                    .orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 3);

            String accessToken = generateUserAccessToken(currentUserId);

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.multipart(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지"),
                                    fieldWithPath("message.description").ignored(),
                                    fieldWithPath("message.title").ignored()
                            )
                    )
            );
        }

        @Test
        @DisplayName("수정하려는 코스가 없는 코스이면, Http Status 400 반환한다.")
        void noCourseError() throws Exception {
            //given
            Long currentUserId = 1L;
            Long courseId = 4L;

            String title = "modifiedTitle";
            String description = "modifiedDescription";

            String accessToken = generateUserAccessToken(currentUserId);

            MockMultipartFile mockMultipartFile = getMockMultipartFile("test-img2.png");

            // mocking
            given(courseQueryService.getStoredFileName(courseId))
                    .willThrow(new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId));

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.multipart(path, courseId)
                            .file(mockMultipartFile)
                            .param("title", title)
                            .param("description", description)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청한 사용자가 해당 코스의 작성자가 아니면, Http Status 403 반환한다.")
        void notWriterError() throws Exception {
            //given
            Long writerId = 3L;
            Course course = setCourses(writerId, 1).stream()
                    .findFirst()
                    .orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 3);

            String title = "modifiedTitle";
            String description = "modifiedDescription";

            Long currentUserId = 1L;

            String accessToken = generateUserAccessToken(currentUserId);

            MockMultipartFile mockMultipartFile = getMockMultipartFile("test-img2.png");

            // mocking
            given(courseQueryService.getStoredFileName(courseId)).willReturn(course.getCourseImage().getStoredName());
            willThrow(new CustomException("해당 코스의 작성자가 아니기 때문에 요청을 수행할 수 없습니다.", ErrorCode.NO_AUTHORITIES))
                    .given(courseService).modifyCourse(eq(courseId), any());

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.multipart(path, courseId)
                            .file(mockMultipartFile)
                            .param("title", title)
                            .param("description", description)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("코스 삭제")
    class courseRemove {

        @Test
        @DisplayName("코스 삭제에 성공하면, 삭제 성공 메시지를 응답한다.")
        void success() throws Exception {
            //given
            Long currentUserId = 1L;
            Course course = setCourses(currentUserId, 1).stream()
                    .findFirst()
                    .orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 3);

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getStoredFileName(courseId)).willReturn(course.getCourseImage().getStoredName());
            willDoNothing().given(courseService).removeCourse(courseId, currentUserId);

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("삭제할 코스 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("코스 삭제 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("경로 파라미터로 받은 식별자를 가진 코스가 없으면 Http status 400 반환한다.")
        void noCourseError() throws Exception {
            //given
            Long currentUserId = 1L;
            Long courseId = 3L;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getStoredFileName(courseId))
                    .willThrow(new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId));

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청한 유저가 삭제하려는 코스의 작성자가 아니면 Http status 403 반환한다.")
        void notWriter() throws Exception {
            //given
            Long courseWriterId = 3L;
            Course course = setCourses(courseWriterId, 1).stream()
                    .findFirst()
                    .orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 3);

            Long currentUserId = 1L;
            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(courseQueryService.getStoredFileName(courseId)).willReturn(course.getCourseImage().getStoredName());
            willThrow(new CustomException("해당 코스의 작성자가 아니기 때문에 요청을 수행할 수 없습니다.", ErrorCode.NO_AUTHORITIES))
                    .given(courseService).removeCourse(courseId, currentUserId);

            //when
            String path = "/courses/{courseId}";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.delete(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            //then
            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("코스 좋아요 등록/취소")
    class courseLikeUpdate {

        private void mockingUpdateCourseLikeSuccess(Course course, Long currentUserId) {
            Long courseId = course.getId();
            given(courseLikeService.updateCourseLike(courseId, currentUserId))
                    .will(
                            invocation -> {
                                CourseLike like = getCourseLikeList().stream()
                                        .filter(courseLike -> courseLike.getCourse().equals(course)
                                                && courseLike.getUserId().equals(currentUserId))
                                        .findFirst()
                                        .orElse(null);

                                if (like != null) {
                                    return null;
                                } else {
                                    if (!course.isWritingComplete()) {
                                        throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + course.getId(), ErrorCode.CAN_NOT_ACCESS_RESOURCE);
                                    }

                                    setCourseLike(
                                            course,
                                            currentUserId
                                    );

                                    return getCourseLikeList().stream()
                                            .filter(courseLike -> courseLike.getCourse().equals(course)
                                                    && courseLike.getUserId().equals(currentUserId))
                                            .findFirst()
                                            .orElseThrow()
                                            .getId();
                                }
                            }
                    );
        }

        @Test
        @DisplayName("현재 유저가 코스에 좋아요를 등록하지 않았을 경우, 좋아요를 등록하고 userLiked = true 를 응답한다.")
        void successCreated() throws Exception {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 3);
            Long courseId = course.getId();
            Long currentUserId = 3L;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            mockingUpdateCourseLikeSuccess(course, currentUserId);

            // when
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userLiked").value(true));

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("좋아요 상태 변경 후, 해당 코스에 대한 유저의 좋아요 여부")
                            )
                    )
            );
        }

        @Test
        @DisplayName("현재 유저가 코스에 좋아요를 등록했었던 경우, 좋아요를 삭제하고 userLiked = false를 응답한다.")
        void successDeleted() throws Exception {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 3);

            Long courseId = course.getId();
            Long currentUserId = 3L;

            setCourseLike(course, currentUserId);

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            mockingUpdateCourseLikeSuccess(course, currentUserId);

            // when
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userLiked").value(false));

            // docs
            perform.andDo(
                    restDocs.document(
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
                                    fieldWithPath("userLiked").type(JsonFieldType.BOOLEAN).description("좋아요 상태 변경 후, 해당 코스에 대한 유저의 좋아요 여부")
                            )
                    )
            );
        }

        @Test
        @DisplayName("경로 파라미터로 넘어온 코스 식별값과 일치하는 코스가 없으면, http status 400 오류를 반환한다.")
        void invalidCourseId() throws Exception {
            // given
            Long courseId = 100L;
            Long userId = 1L;

            given(courseLikeService.updateCourseLike(courseId, userId))
                    .willThrow(new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId));

            String accessToken = generateUserAccessToken(userId);

            // when
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("현재 유저가 요청한 대상 코스의 상태가 '작성 완료'가 아니고, 현재 유저가 해당 코스에 좋아요 한 적이 없으면, http status 400 반환한다.")
        void notCompleteCourseError() throws Exception {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();
            Long currentUserId = 3L;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            mockingUpdateCourseLikeSuccess(course, currentUserId);

            // when
            String path = "/courses/{courseId}/like";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").exists())
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        // TODO 로그인하지 않은 유저는 사용 불가
    }
}
