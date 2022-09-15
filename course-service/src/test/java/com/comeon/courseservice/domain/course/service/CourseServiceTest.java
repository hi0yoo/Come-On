package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
public class CourseServiceTest {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseLikeRepository courseLikeRepository;

    @SpyBean
    CourseService courseService;

    @Nested
    @DisplayName("코스 등록")
    class saveCourse {

        @Test
        @DisplayName("코스 저장에 성공한다.")
        void success() {
            // given
            Long userId = 1L;
            CourseDto courseDto = new CourseDto(
                    userId,
                    "title",
                    "description",
                    new CourseImageDto("originalName", "storedName")
            );

            // when
            Long savedCourseId = courseService.saveCourse(courseDto);

            // then
            Optional<Course> optionalCourse = courseRepository.findById(savedCourseId);
            assertThat(optionalCourse).isPresent();
            Course course = optionalCourse.orElseThrow();
            assertThat(course.getId()).isEqualTo(savedCourseId);
            assertThat(course.getTitle()).isEqualTo(courseDto.getTitle());
            assertThat(course.getDescription()).isEqualTo(courseDto.getDescription());
            assertThat(course.getCourseImage().getId()).isNotNull();
            assertThat(course.getCourseImage().getOriginalName()).isEqualTo(courseDto.getCourseImageDto().getOriginalName());
            assertThat(course.getCourseImage().getStoredName()).isEqualTo(courseDto.getCourseImageDto().getStoredName());
        }
    }

    @Nested
    @DisplayName("코스 수정")
    class modifyCourse {

        Long userId = 1L;
        Long courseId;

        @BeforeEach
        void initCourse() {
            Course course = courseRepository.save(
                    Course.builder()
                            .userId(userId)
                            .title("title")
                            .description("description")
                            .courseImage(
                                    CourseImage.builder()
                                            .originalName("originalName")
                                            .storedName("storedName")
                                            .build()
                            )
                            .build()
            );
            courseId =  course.getId();
        }

        @Test
        @DisplayName("넘어온 데이터에 코스 이미지 정보가 존재하면 이미지 정보를 함께 수정한다.")
        void success() {
            // given
            CourseDto courseDto = new CourseDto(
                    userId,
                    "modifiedTitle",
                    "modifiedDescription",
                    new CourseImageDto("modifiedOriginalName", "modifiedStoredName")
            );

            // when
            courseService.modifyCourse(courseId, courseDto);

            // then
            Course course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.getTitle()).isEqualTo(courseDto.getTitle());
            assertThat(course.getDescription()).isEqualTo(courseDto.getDescription());
            assertThat(course.getCourseImage().getOriginalName()).isEqualTo(courseDto.getCourseImageDto().getOriginalName());
            assertThat(course.getCourseImage().getStoredName()).isEqualTo(courseDto.getCourseImageDto().getStoredName());
        }

        @Test
        @DisplayName("넘어온 데이터에 코스 이미지 정보가 존재하지 않으면 코스 정보만 수정한다.")
        void successNotModifyCourseImage() {
            // given
            Course course = courseRepository.findById(courseId).orElseThrow();

            String originalName = course.getCourseImage().getOriginalName();
            String storedName = course.getCourseImage().getStoredName();

            CourseDto courseDto = new CourseDto(
                    userId,
                    "modifiedTitle",
                    "modifiedDescription",
                    null
            );

            // when
            courseService.modifyCourse(courseId, courseDto);

            // then
            assertThat(course.getTitle()).isEqualTo(courseDto.getTitle());
            assertThat(course.getDescription()).isEqualTo(courseDto.getDescription());
            assertThat(course.getCourseImage().getOriginalName()).isEqualTo(originalName);
            assertThat(course.getCourseImage().getStoredName()).isEqualTo(storedName);
        }

        @Test
        @DisplayName("courseId와 매칭되는 코스 엔티티가 없으면 EntityNotFoundException 발생")
        void noCourseEntity() {
            // given
            Long invalidCourseId = 100L;

            CourseDto courseDto = new CourseDto(
                    userId,
                    "modifiedTitle",
                    "modifiedDescription",
                    null
            );

            // when, then
            assertThatThrownBy(
                    () -> courseService.modifyCourse(invalidCourseId, courseDto)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("CourseDto.userId가 해당 코스의 작성자와 일치하지 않으면, CustomException 발생한다. 내부에 ErrorCode.NO_AUTHORITIES")
        void notWriter() {
            // given
            Long invalidUserId = 100L;
            CourseDto courseDto = new CourseDto(
                    invalidUserId,
                    "modifiedTitle",
                    "modifiedDescription",
                    null
            );

            // when, then
            assertThatThrownBy(
                    () -> courseService.modifyCourse(courseId, courseDto)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }

    @Nested
    @DisplayName("코스 삭제")
    class removeCourse {

        Long userId = 1L;
        Long courseId;

        @BeforeEach
        void initData() {
            Course course = Course.builder()
                    .userId(userId)
                    .title("title")
                    .description("description")
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("originalName")
                                    .storedName("storedName")
                                    .build()
                    )
                    .build();

            for (int i = 1; i <= 3; i++) {
                CoursePlace.builder()
                        .course(course)
                        .name("placeName" + i)
                        .description("placeDescription" + i)
                        .lat(nextDouble() * (38 - 36 + 1) + 36)
                        .lng(nextDouble() * (128 - 126 + 1) + 126)
                        .order(i)
                        .kakaoPlaceId((long) (i + 10000))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
            }

            courseId = courseRepository.save(course).getId();

            for (int i = 1; i <= 10; i++) {
                CourseLike courseLike = CourseLike.builder()
                        .course(course)
                        .userId((long) i)
                        .build();
                courseLikeRepository.save(courseLike);
            }
        }

        @Test
        @DisplayName("존재하는 코스의 식별값과 해당 코스의 작성자 식별값이 파라미터로 들어오면 코스 삭제에 성공한다. 코스 삭제시 연관된 좋아요, 장소, 이미지를 모두 삭제한다.")
        void success() {
            // given

            // when
            courseService.removeCourse(courseId, userId);

            // then
            assertThat(courseRepository.findById(courseId).isPresent()).isFalse();
            assertThat(courseLikeRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 파라미터로 들어오면, EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long invalidCourseId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourse(invalidCourseId, userId)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("해당 코스의 작성자가 아닌 유저의 식별값이 파라미터로 들어오면, CustomException 발생한다. 내부에 ErrorCode.NO_AUTHORITIES 가진다.")
        void notWriter() {
            // given
            long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourse(courseId, invalidUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }
}
