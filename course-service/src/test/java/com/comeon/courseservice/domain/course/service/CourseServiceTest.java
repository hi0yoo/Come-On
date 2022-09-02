package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseWriteStatus;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
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
}
