package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseLike;
import com.comeon.courseservice.domain.course.repository.CourseLikeRepository;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import com.comeon.courseservice.web.feign.userservice.UserServiceFeignClient;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("test")
@Import({S3MockConfig.class})
class CourseQueryServiceTest {

    @Value("${s3.folder-name.course}")
    String dirName;

    @Autowired
    FileManager fileManager;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseLikeRepository courseLikeRepository;

    @Autowired
    EntityManager em;

    @MockBean
    UserServiceFeignClient userServiceFeignClient;

    @Autowired
    CourseQueryService courseQueryService;

    Course course;

    @Value("${jwt.secret}")
    String jwtSecretKey;

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

    @Nested
    @DisplayName("코스 단건 조회")
    class getCourseDetails {

        private void setUserServiceFeignClientMock(Long userId) {
            given(userServiceFeignClient.getUserDetails(userId))
                    .willReturn(ApiResponse.createSuccess(
                            new UserDetailsResponse(
                                    userId,
                                    "userNickname",
                                    "userProfileImgUrl")
                    ));
        }

        @Test
        @DisplayName("작성 완료된 코스의 식별값이 들어오면 코스 데이터 조회에 성공한다.")
        void success() throws IOException {
            // given
            initCourseAndPlaces();
            course.completeWriting(); // 코스 작성 완료
            initCourseLikes(); // 코스 좋아요 추가
            em.flush();
            em.clear();

            Long courseId = course.getId();

            Long userId = course.getUserId();
            setUserServiceFeignClientMock(userId);

            // when
            CourseDetailResponse courseDetails = courseQueryService.getCourseDetails(courseId, userId);

            System.out.println(userServiceFeignClient.getUserDetails(userId).getData().getProfileImgUrl());

            // then
            assertThat(courseDetails).isNotNull();
            assertThat(courseDetails.getCourseId()).isEqualTo(courseId);
            assertThat(courseDetails.getTitle()).isEqualTo(course.getTitle());
            assertThat(courseDetails.getDescription()).isEqualTo(course.getDescription());

            assertThat(courseDetails.getWriter()).isNotNull();
            assertThat(courseDetails.getWriter().getUserId()).isEqualTo(course.getUserId());
            assertThat(courseDetails.getWriter().getNickname()).isNotNull();

            assertThat(courseDetails.getLikeCount()).isEqualTo(course.getLikeCount());

            assertThat(courseDetails.getUserLikeId()).isNotNull();

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
        void successSameWriterWhenWritingNotComplete() throws IOException {
            // given
            initCourse();
            em.flush();
            em.clear();

            Long courseId = course.getId();

            Long userId = course.getUserId();
            setUserServiceFeignClientMock(userId);

            // when
            CourseDetailResponse courseDetails = courseQueryService.getCourseDetails(courseId, userId);

            System.out.println(userServiceFeignClient.getUserDetails(userId).getData().getProfileImgUrl());

            // then
            assertThat(courseDetails).isNotNull();
            assertThat(courseDetails.getCourseId()).isEqualTo(courseId);
            assertThat(courseDetails.getTitle()).isEqualTo(course.getTitle());
            assertThat(courseDetails.getDescription()).isEqualTo(course.getDescription());

            assertThat(courseDetails.getWriter()).isNotNull();
            assertThat(courseDetails.getWriter().getUserId()).isEqualTo(course.getUserId());
            assertThat(courseDetails.getWriter().getNickname()).isNotNull();
            assertThat(courseDetails.getCoursePlaces()).isEmpty();
        }

        @Test
        @DisplayName("작성 완료되지 않은 코스의 식별값과, 해당 코스 작성자가 아닌 유저 식별값이 들어오면, CustomException 발생한다. ErrorCode.NO_AUTHORITIES")
        void failNotCompleteCourse() throws IOException {
            // given
            initCourseAndPlaces();
            em.flush();
            em.clear();
            Long courseId = course.getId();

            Long userId = course.getUserId();
            setUserServiceFeignClientMock(userId);

            Long currentUserId = 500L;

            // when, then
            assertThatThrownBy(
                    () -> courseQueryService.getCourseDetails(courseId, currentUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면, EntityNotFoundException 발생한다.")
        void failEntityNotFound() throws IOException {
            // given
            Long invalidCourseId = 100L;

            Long userId = 100L;
            setUserServiceFeignClientMock(userId);

            // when, then
            assertThatThrownBy(
                    () -> courseQueryService.getCourseDetails(invalidCourseId, userId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}