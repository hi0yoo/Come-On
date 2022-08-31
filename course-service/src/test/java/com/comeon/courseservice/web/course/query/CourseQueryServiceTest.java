package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.config.MockFeignClientConfig;
import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
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

@Slf4j
@Transactional
@SpringBootTest
@Import({S3MockConfig.class, MockFeignClientConfig.class})
class CourseQueryServiceTest {

    @Value("${s3.folder-name.course}")
    String dirName;

    @Autowired
    FileManager fileManager;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    EntityManager em;

    @Autowired
    @Qualifier("mockCourseQueryService")
    CourseQueryService mockCourseQueryService;

    @Autowired
    @Qualifier("courseQueryService")
    CourseQueryService originalCourseQueryService;

    Course course;

    void initCourseAndPlaces() throws IOException {
        Long userId = 1L;
        String title = "courseTitle";
        String description = "courseDescription";

        File imgFile = ResourceUtils.getFile(this.getClass().getResource("/static/test-img2.png"));
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "imgFile",
                "test-img2.png",
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

    String jwtSecretKey = "8490783c21034fd55f9cde06d539607f326356fa9732d93db12263dc4ce906a02ab20311228a664522bf7ed3ff66f0b3694e94513bdfa17bc631e57030c248ed";

    @Disabled
    @Test
    @DisplayName("유저 서비스 연동 테스트")
    void test() throws IOException {
        // given
        initCourseAndPlaces();
        course.completeWriting(); // 코스 작성 완료
        em.flush();
        em.clear();

        Long courseId = course.getId();

        // when
        CourseDetailResponse courseDetails = originalCourseQueryService.getCourseDetails(courseId);

        // then
        assertThat(courseDetails).isNotNull();
        assertThat(courseDetails.getCourseId()).isEqualTo(courseId);
        assertThat(courseDetails.getTitle()).isEqualTo(course.getTitle());
        assertThat(courseDetails.getDescription()).isEqualTo(course.getDescription());

        assertThat(courseDetails.getWriter()).isNotNull();
        assertThat(courseDetails.getWriter().getUserId()).isEqualTo(course.getUserId());
        assertThat(courseDetails.getWriter().getNickname()).isNotNull();

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

    @Nested
    @DisplayName("코스 단건 조회")
    class getCourseDetails {

        @Test
        @DisplayName("작성 완료된 코스의 식별값이 들어오면 코스 데이터 조회에 성공한다.")
        void success() throws IOException {
            // given
            initCourseAndPlaces();
            course.completeWriting(); // 코스 작성 완료
            em.flush();
            em.clear();

            Long courseId = course.getId();

            // when
            CourseDetailResponse courseDetails = mockCourseQueryService.getCourseDetails(courseId);

            // then
            assertThat(courseDetails).isNotNull();
            assertThat(courseDetails.getCourseId()).isEqualTo(courseId);
            assertThat(courseDetails.getTitle()).isEqualTo(course.getTitle());
            assertThat(courseDetails.getDescription()).isEqualTo(course.getDescription());

            assertThat(courseDetails.getWriter()).isNotNull();
            assertThat(courseDetails.getWriter().getUserId()).isEqualTo(course.getUserId());
            assertThat(courseDetails.getWriter().getNickname()).isNotNull();

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
        @DisplayName("작성 완료되지 않은 코스의 식별값이 들어오면, CustomException 발생한다. ErrorCode.ENTITY_NOT_FOUND")
        void failNotCompleteCourse() throws IOException {
            // given
            initCourseAndPlaces();
            em.flush();
            em.clear();
            Long courseId = course.getId();

            // when, then
            assertThatThrownBy(
                    () -> mockCourseQueryService.getCourseDetails(courseId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면, EntityNotFoundException 발생한다.")
        void failEntityNotFound() throws IOException {
            // given
            Long invalidCourseId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> mockCourseQueryService.getCourseDetails(invalidCourseId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}