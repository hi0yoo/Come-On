package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.AbstractQueryServiceTest;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.common.response.SliceResponse;
import com.comeon.courseservice.web.course.query.repository.CourseLikeQueryRepository;
import com.comeon.courseservice.web.course.query.repository.CourseQueryRepository;
import com.comeon.courseservice.web.course.query.repository.dto.CourseCondition;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import com.comeon.courseservice.web.course.response.CourseListResponse;
import com.comeon.courseservice.web.course.response.MyPageCourseListResponse;
import com.comeon.courseservice.web.feign.userservice.UserFeignService;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import com.comeon.courseservice.web.feign.userservice.response.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@Slf4j
class CourseQueryServiceTest extends AbstractQueryServiceTest {

    @Autowired
    CourseQueryRepository courseQueryRepository;

    @Autowired
    CourseLikeQueryRepository courseLikeQueryRepository;

    @MockBean
    UserFeignService userFeignService;

    @SpyBean
    CourseQueryService courseQueryService;

    void mockUserDetails(Long userId) {
        given(userFeignService.getUserDetails(userId))
                .willReturn(Optional.of(
                                new UserDetailsResponse(
                                        userId,
                                        "userNickname",
                                        "userProfileImgUrl",
                                        UserStatus.ACTIVATE
                                )
                        )
                );
    }

    void mockUserDetailsMap() {
        Map<Long, UserDetailsResponse> userDetailsResponseMap = new HashMap<>();
        given(userFeignService.getUserDetailsMap(anyList()))
                .willReturn(userDetailsResponseMap);
    }

    @Nested
    @DisplayName("코스 단건 조회")
    class getCourseDetails {

        @Test
        @DisplayName("작성 완료된 코스의 식별값이 들어오면 코스 데이터 조회에 성공한다.")
        void success() {
            // given
            Long courseId = 1L;
            Long userId = 1L;

            // mocking
            mockUserDetails(userId);

            // when
            CourseDetailResponse courseDetails = courseQueryService.getCourseDetails(courseId, userId);

            // then
            assertThat(courseDetails).isNotNull();
            assertThat(courseDetails.getCourseId()).isEqualTo(courseId);

            Course course = courseRepository.findById(courseId).orElseThrow();
            assertThat(courseDetails.getTitle()).isEqualTo(course.getTitle());
            assertThat(courseDetails.getDescription()).isEqualTo(course.getDescription());

            assertThat(courseDetails.getWriter()).isNotNull();
            assertThat(courseDetails.getWriter().getUserId()).isEqualTo(course.getUserId());
            assertThat(courseDetails.getWriter().getNickname()).isNotNull();

            assertThat(courseDetails.getLikeCount()).isEqualTo(course.getLikeCount());

            assertThat(courseDetails.getUserLiked()).isNotNull();

            List<CoursePlace> coursePlaces = course.getCoursePlaces();
            for (CourseDetailResponse.CoursePlaceDetailInfo coursePlaceDetailInfo : courseDetails.getCoursePlaces()) {
                CoursePlace matchCoursePlace = coursePlaces.stream()
                        .filter(coursePlace -> coursePlace.getOrder().equals(coursePlaceDetailInfo.getOrder()))
                        .findFirst()
                        .orElse(null);

                assertThat(coursePlaceDetailInfo).isNotNull();
                assertThat(coursePlaceDetailInfo.getCoursePlaceId()).isEqualTo(matchCoursePlace.getId());
                assertThat(coursePlaceDetailInfo.getName()).isEqualTo(matchCoursePlace.getName());
                assertThat(coursePlaceDetailInfo.getDescription()).isEqualTo(matchCoursePlace.getDescription());
                assertThat(coursePlaceDetailInfo.getLat()).isEqualTo(matchCoursePlace.getLat());
                assertThat(coursePlaceDetailInfo.getLng()).isEqualTo(matchCoursePlace.getLng());
            }
        }

        @Test
        @DisplayName("작성이 완료되지 않았더라도, 코스의 작성자 식별값이 함께 들어오면, 조회에 성공한다.")
        void successSameWriterWhenWritingNotComplete() {
            // given
            Long userId = 10L;
            UploadedFileInfo uploadedFileInfo = fileUpload();
            Course course = Course.builder()
                    .userId(userId)
                    .title("title" + userId)
                    .description("description" + userId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName(uploadedFileInfo.getOriginalFileName() + userId)
                                    .storedName(uploadedFileInfo.getStoredFileName() + userId)
                                    .build()
                    )
                    .build();
            courseRepository.save(course);
            Long courseId = course.getId();

            // mocking
            mockUserDetails(userId);

            // when
            CourseDetailResponse courseDetails = courseQueryService.getCourseDetails(courseId, userId);

            // then
            assertThat(courseDetails).isNotNull();
            assertThat(courseDetails.getCourseId()).isEqualTo(courseId);

            Course foundCourse = courseRepository.findById(courseId).orElseThrow();
            assertThat(courseDetails.getTitle()).isEqualTo(foundCourse.getTitle());
            assertThat(courseDetails.getDescription()).isEqualTo(foundCourse.getDescription());

            assertThat(courseDetails.getWriter()).isNotNull();
            assertThat(courseDetails.getWriter().getUserId()).isEqualTo(foundCourse.getUserId());
            assertThat(courseDetails.getWriter().getNickname()).isNotNull();
            assertThat(courseDetails.getCoursePlaces()).isEmpty();
        }

        @Test
        @DisplayName("작성이 완료되지 않은 코스에 작성자가 아닌 유저 식별자로 조회하면 CustomException 발생한다.")
        void notCompleteError() {
            // given
            Long userId = 10L;
            UploadedFileInfo uploadedFileInfo = fileUpload();
            Course course = Course.builder()
                    .userId(userId)
                    .title("title" + userId)
                    .description("description" + userId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName(uploadedFileInfo.getOriginalFileName() + userId)
                                    .storedName(uploadedFileInfo.getStoredFileName() + userId)
                                    .build()
                    )
                    .build();
            courseRepository.save(course);

            Long courseId = course.getId();
            Long invalidUserId = 300L;

            // when, then
            assertThatThrownBy(
                    () -> courseQueryService.getCourseDetails(courseId, invalidUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면, EntityNotFoundException 발생한다.")
        void failEntityNotFound() {
            // given
            Long invalidCourseId = 100L;
            Long userId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseQueryService.getCourseDetails(invalidCourseId, userId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("코스 리스트 조회")
    class getCourseList {

        @Test
        @DisplayName("검색 조건에 검색어가 들어오면 제목에 검색어가 포함된 코스들만 조회된다. " +
                "검색 조건에 좌표값이 있으면 위치 기준 100km 이내의 장소들만 조회한다.")
        void successAllCondition() {
            // given
            String titleCondition = "3";
            CourseCondition courseCondition = new CourseCondition(titleCondition, 37.555, 126.972);
            Long userId = 5L;

            // mocking
            mockUserDetailsMap();

            // when
            SliceResponse<CourseListResponse> courseList = courseQueryService.getCourseList(userId, courseCondition, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (CourseListResponse content : courseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());
                log.info("firstPlace.id : {}", content.getFirstPlace().getCoursePlaceId());
                log.info("firstPlace.lat : {}", content.getFirstPlace().getLat());
                log.info("firstPlace.lng : {}", content.getFirstPlace().getLng());
                log.info("firstPlace.distance : {}", content.getFirstPlace().getDistance());

                log.info("================");
            }

            List<CourseListResponse> contents = courseList.getContents();
            assertThat(contents.stream()
                    .filter(courseListResponse -> courseListResponse.getFirstPlace().getDistance() > 100)
                    .findAny())
                    .isNotPresent();

            assertThat(contents.stream()
                    .allMatch(courseListResponse -> courseListResponse.getTitle().contains(titleCondition)))
                    .isTrue();

            assertThat(contents.stream()
                    .allMatch(courseListResponse -> courseListResponse.getCourseStatus().equals(CourseStatus.COMPLETE)))
                    .isTrue();
        }

        @Test
        @DisplayName("검색 조건이 없으면 서울역 좌표 기준으로 100km 이내의 코스들을 검색한다.")
        void successNoCondition() {
            // given
            CourseCondition courseCondition = new CourseCondition(null, null, null);
            Long userId = 5L;

            // mocking
            mockUserDetailsMap();

            // when
            SliceResponse<CourseListResponse> courseList = courseQueryService.getCourseList(userId, courseCondition, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (CourseListResponse content : courseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());
                log.info("firstPlace.id : {}", content.getFirstPlace().getCoursePlaceId());
                log.info("firstPlace.lat : {}", content.getFirstPlace().getLat());
                log.info("firstPlace.lng : {}", content.getFirstPlace().getLng());
                log.info("firstPlace.distance : {}", content.getFirstPlace().getDistance());

                log.info("================");
            }

            List<CourseListResponse> contents = courseList.getContents();
            assertThat(contents.stream()
                    .filter(courseListResponse -> courseListResponse.getFirstPlace().getDistance() > 100)
                    .findAny())
                    .isNotPresent();

            assertThat(contents.stream()
                    .allMatch(courseListResponse -> courseListResponse.getCourseStatus().equals(CourseStatus.COMPLETE)))
                    .isTrue();
        }

        @Test
        @DisplayName("유저 식별값이 null이면 조회 결과의 userLiked 필드는 모두 false 이다.")
        void successNoUserId() {
            // given
            CourseCondition courseCondition = new CourseCondition(null, null, null);
            Long userId = null;

            // mocking
            mockUserDetailsMap();

            // when
            SliceResponse<CourseListResponse> courseList = courseQueryService.getCourseList(userId, courseCondition, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (CourseListResponse content : courseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());
                log.info("firstPlace.id : {}", content.getFirstPlace().getCoursePlaceId());
                log.info("firstPlace.lat : {}", content.getFirstPlace().getLat());
                log.info("firstPlace.lng : {}", content.getFirstPlace().getLng());
                log.info("firstPlace.distance : {}", content.getFirstPlace().getDistance());

                log.info("================");
            }

            List<CourseListResponse> contents = courseList.getContents();
            assertThat(contents.stream()
                    .filter(courseListResponse -> courseListResponse.getFirstPlace().getDistance() > 100)
                    .findAny())
                    .isNotPresent();

            assertThat(contents.stream()
                    .allMatch(courseListResponse -> courseListResponse.getCourseStatus().equals(CourseStatus.COMPLETE)))
                    .isTrue();

            assertThat(contents.stream()
                    .allMatch(courseListResponse -> courseListResponse.getUserLiked().equals(false)))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("현재 사용자가 등록한 코스 리스트 조회")
    class getMyRegisteredCourseList {

        @Test
        @DisplayName("조회 결과의 writer 식별자 필드는 모두 userId와 같다.")
        void writerIdIsSameUserId() {
            Long userId = 3L;

            // mocking
            mockUserDetails(userId);

            // when
            SliceResponse<MyPageCourseListResponse> myRegisteredCourseList = courseQueryService.getMyRegisteredCourseList(userId, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (MyPageCourseListResponse content : myRegisteredCourseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());

                log.info("================");
            }

            List<MyPageCourseListResponse> contents = myRegisteredCourseList.getContents();
            assertThat(contents.stream()
                    .allMatch(myPageCourseListResponse -> myPageCourseListResponse.getWriter().getUserId().equals(userId)))
                    .isTrue();
        }

        @Test
        @DisplayName("작성완료되지 않은 코스도 조회한다.")
        void includeNotCompleteCourse() {
            // given
            Long userId = 3L;

            // 가장 최신에 미완료 코스 추가
            Course course = Course.builder()
                    .title("notCompleteCourse")
                    .description("notCompleteCourse")
                    .userId(userId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("notCompleteImg")
                                    .storedName("notCompleteImgStoredName")
                                    .build()
                    )
                    .build();
            courseRepository.save(course);

            // mocking
            mockUserDetails(userId);

            // when
            SliceResponse<MyPageCourseListResponse> myRegisteredCourseList = courseQueryService.getMyRegisteredCourseList(userId, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (MyPageCourseListResponse content : myRegisteredCourseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());

                log.info("================");
            }

            List<MyPageCourseListResponse> contents = myRegisteredCourseList.getContents();
            assertThat(contents.stream()
                    .allMatch(myPageCourseListResponse -> myPageCourseListResponse.getWriter().getUserId().equals(userId)))
                    .isTrue();

            // 가장 최신에 있으므로 조회 O
            assertThat(contents.stream()
                    .filter(myPageCourseListResponse -> myPageCourseListResponse.getCourseStatus().equals(CourseStatus.WRITING))
                    .findAny())
                    .isPresent();
        }
    }

    @Nested
    @DisplayName("현재 사용자가 좋아요한 코스 리스트 조회")
    class getMyLikedCourseList {

        @Test
        @DisplayName("모든 userLiked 필드는 true 이다.")
        void allUserLikedFieldsTrue() {
            // given
            Long userId = 5L;

            // mocking
            mockUserDetailsMap();

            // when
            SliceResponse<MyPageCourseListResponse> myRegisteredCourseList = courseQueryService.getMyLikedCourseList(userId, PageRequest.of(0, 10));

            // then
            log.info("content\n============");
            for (MyPageCourseListResponse content : myRegisteredCourseList.getContents()) {
                log.info("courseId : {}", content.getCourseId());
                log.info("title : {}", content.getTitle());
                log.info("imageUrl : {}", content.getImageUrl());
                log.info("status : {}", content.getCourseStatus());
                log.info("lastModifiedDate : {}", content.getLastModifiedDate());
                log.info("writer.id : {}", content.getWriter().getUserId());
                log.info("writer.nickname : {}", content.getWriter().getNickname());
                log.info("likeCount : {}", content.getLikeCount());
                log.info("userLiked : {}", content.getUserLiked());

                log.info("================");
            }

            List<MyPageCourseListResponse> contents = myRegisteredCourseList.getContents();
            assertThat(contents.stream()
                    .allMatch(myPageCourseListResponse -> myPageCourseListResponse.getUserLiked().equals(true)))
                    .isTrue();
        }
    }
}