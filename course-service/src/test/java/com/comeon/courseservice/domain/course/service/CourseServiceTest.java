package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseWriteStatus;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseServiceTest {

    @Autowired
    CourseService courseService;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CoursePlaceRepository coursePlaceRepository;

    @Autowired
    EntityManager em;

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
    @DisplayName("코스 등록 - 코스 이미지, 코스 제목, 코스 설명, 유저 아이디가 주어진다")
    class saveCourse {

        @Test
        @DisplayName("코스 등록에 성공하면, 등록된 코스의 식별값을 반환한다. 코스는 작성중 상태로 저장된다.")
        void success() {
            // given
            Long userId = 1L;
            String title = "courseTitle";
            String description = "courseDescription";
            CourseImageDto courseImageDto = CourseImageDto.builder()
                    .originalName("originalName")
                    .storedName("storedName")
                    .build();

            CourseDto courseDto = CourseDto.builder()
                    .userId(userId)
                    .title(title)
                    .description(description)
                    .courseImageDto(courseImageDto)
                    .build();

            // when
            Long savedCourseId = courseService.saveCourse(courseDto);

            // then
            Course course = courseRepository.findById(savedCourseId).orElse(null);
            assertThat(course).isNotNull();
            assertThat(course.getTitle()).isEqualTo(title);
            assertThat(course.getDescription()).isEqualTo(description);
            assertThat(course.getUserId()).isEqualTo(userId);

            assertThat(course.getCourseImage()).isNotNull();
            assertThat(course.getCourseImage().getId()).isNotNull();
            assertThat(course.getCourseImage().getOriginalName()).isEqualTo(courseImageDto.getOriginalName());
            assertThat(course.getCourseImage().getStoredName()).isEqualTo(courseImageDto.getStoredName());

            // 등록된 코스는 작성중 상태여야 한다.
            assertThat(course.getWriteStatus()).isEqualTo(CourseWriteStatus.WRITING);

            assertThat(course.getCoursePlaces().size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("코스 작성 완료")
    class completeWritingCourse {

        @Test
        @DisplayName("코스 장소가 등록되어 있으면, 코스 작성 상태를 완료로 변경한다.")
        void success() {
            // given
            initCourse();
            Long userId = course.getUserId();
            Long courseId = course.getId();

            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name("placeName")
                    .description("placeDescription")
                    .lat(1.1)
                    .lng(1.3)
                    .order(1)
                    .build();
            coursePlaceRepository.save(coursePlace);
            em.flush();
            em.clear();

            // when
            courseService.completeWritingCourse(courseId, userId);
            em.flush();
            em.clear();

            // then
            Course findCourse = courseRepository.findById(courseId).orElse(null);
            assertThat(findCourse).isNotNull();
            assertThat(findCourse.getWriteStatus()).isNotEqualTo(CourseWriteStatus.WRITING);
            assertThat(findCourse.getWriteStatus()).isEqualTo(CourseWriteStatus.COMPLETE);
        }
    }

    @Test
    @DisplayName("등록된 코스 장소가 하나도 없으면, ErrorCode.VALIDATION_FAIL 필드를 가진 CustomException 예외가 발생한다.")
    void fail_1() {
        // given
        initCourse();
        Long userId = course.getUserId();
        Long courseId = course.getId();
        em.flush();
        em.clear();

        // when, then
        assertThatThrownBy(
                () -> courseService.completeWritingCourse(courseId, userId)
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAIL);
    }

    @Test
    @DisplayName("코스를 등록한 유저와 상태 변경하려는 유저가 다르면, ErrorCode.NO_AUTHORITIES 필드를 가진 CustomException 예외가 발생한다.")
    void fail_2() {
        // given
        initCourse();
        Long invalidUserId = 100L;
        Long courseId = course.getId();

        CoursePlace coursePlace = CoursePlace.builder()
                .course(course)
                .name("placeName")
                .description("placeDescription")
                .lat(1.1)
                .lng(1.3)
                .order(1)
                .build();
        coursePlaceRepository.save(coursePlace);
        em.flush();
        em.clear();

        // when, then
        assertThatThrownBy(
                () -> courseService.completeWritingCourse(courseId, invalidUserId)
        )
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
    }

    @Test
    @DisplayName("파라미터로 넘어온 courseId와 일치하는 코스 식별자가 없으면, EntityNotFoundException 예외를 발생시킨다.")
    void fail_3() {
        // given
        Long invalidCourseId = 100L;
        Long userId = 1L;

        // when, then
        assertThatThrownBy(
                () -> courseService.completeWritingCourse(invalidCourseId, userId)
        ).isInstanceOf(EntityNotFoundException.class);
    }
}
