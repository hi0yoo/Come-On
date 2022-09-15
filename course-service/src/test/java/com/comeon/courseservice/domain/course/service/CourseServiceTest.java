package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.AbstractServiceTest;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Slf4j
public class CourseServiceTest extends AbstractServiceTest {

    @Mock
    CourseRepository courseRepository;

    @Mock
    CourseLikeRepository courseLikeRepository;

    @InjectMocks
    CourseService courseService;

    @Nested
    @DisplayName("코스 등록")
    class saveCourse {

        @Test
        @DisplayName("코스 저장에 성공한다.")
        void success() {
            // given
            Long userId = 1L;
            String title = "title";
            String description = "description";
            CourseImageDto courseImageDto = new CourseImageDto("originalName", "storedName");

            CourseDto courseDto = new CourseDto(userId, title, description, courseImageDto);

            // mocking
            Course course = courseDto.toEntity();
            ReflectionTestUtils.setField(course, "id", 1L);
            ReflectionTestUtils.setField(course.getCourseImage(), "id", 1L);

            when(courseRepository.save(any(Course.class)))
                    .thenReturn(course);

            // when
            Long savedCourseId = courseService.saveCourse(courseDto);

            // then
            assertThat(savedCourseId).isEqualTo(course.getId());
        }
    }

    @Nested
    @DisplayName("코스 수정")
    class modifyCourse {

        @Test
        @DisplayName("넘어온 데이터에 코스 이미지 정보가 존재하면 이미지 정보를 함께 수정한다.")
        void success() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            CourseImageDto courseImageDto = new CourseImageDto("modifiedOriginalName", "modifiedStoredName");
            CourseDto courseDto = new CourseDto(userId, "modifiedTitle", "modifiedDescription", courseImageDto);

            // mocking
            when(courseRepository.findByIdFetchCourseImage(courseId))
                    .thenReturn(Optional.of(course));

            // when
            courseService.modifyCourse(courseId, courseDto);

            // then
            assertThat(course.getTitle()).isEqualTo(courseDto.getTitle());
            assertThat(course.getDescription()).isEqualTo(courseDto.getDescription());
            assertThat(course.getCourseImage().getOriginalName()).isEqualTo(courseDto.getCourseImageDto().getOriginalName());
            assertThat(course.getCourseImage().getStoredName()).isEqualTo(courseDto.getCourseImageDto().getStoredName());
        }

        @Test
        @DisplayName("넘어온 데이터에 코스 이미지 정보가 존재하지 않으면 코스 정보만 수정한다.")
        void successNotModifyCourseImage() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            CourseImage courseImage = course.getCourseImage();
            String originalName = courseImage.getOriginalName();
            String storedName = courseImage.getStoredName();
            CourseDto courseDto = new CourseDto(userId, "modifiedTitle", "modifiedDescription", null);

            // mocking
            when(courseRepository.findByIdFetchCourseImage(courseId))
                    .thenReturn(Optional.of(course));

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
            long userId = 1L;
            long courseId = 1L;

            CourseDto courseDto = new CourseDto(userId, "modifiedTitle", "modifiedDescription", null);

            // mocking
            when(courseRepository.findByIdFetchCourseImage(courseId))
                    .thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(
                    () -> courseService.modifyCourse(courseId, courseDto)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("CourseDto.userId가 해당 코스의 작성자와 일치하지 않으면, CustomException 발생한다. 내부에 ErrorCode.NO_AUTHORITIES")
        void notWriter() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            CourseDto courseDto = new CourseDto(100L, "modifiedTitle", "modifiedDescription", null);

            // mocking
            when(courseRepository.findByIdFetchCourseImage(courseId))
                    .thenReturn(Optional.of(course));

            // when, then
            assertThatThrownBy(
                    () -> courseService.modifyCourse(courseId, courseDto)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }

    @Nested
    @DisplayName("코스 삭제")
    class removeCourse {

        @Test
        @DisplayName("존재하는 코스의 식별값과 해당 코스의 작성자 식별값이 파라미터로 들어오면 코스 삭제에 성공한다. 코스 삭제시 연관된 좋아요, 장소, 이미지를 모두 삭제한다.")
        void success() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();
            setCoursePlaces(course, 5);
            for (int i = 1; i <= 10; i++) {
                setCourseLike(course, (long) i);
            }
            List<CourseLike> courseLikeList = getCourseLikeList().stream()
                    .filter(courseLike -> courseLike.getCourse().equals(course))
                    .collect(Collectors.toList());
            int courseLikeCountWhenBeforeDeleteCourse = courseLikeList.size();

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            doAnswer(invocation -> {
                getCourseLikeList().removeAll(courseLikeList);
                return null;
            }).when(courseLikeRepository).deleteByCourse(course);

            doAnswer(invocation -> {
                getCourseList().remove(course);
                return null;
            }).when(courseRepository).delete(course);

            // when
            courseService.removeCourse(courseId, userId);

            // then
            assertThat(getCourseList().size()).isNotEqualTo(courseLikeCountWhenBeforeDeleteCourse);
            assertThat(getCourseList().size()).isEqualTo(0);
            assertThat(getCourseList().stream().filter(c -> c.getId().equals(courseId)).findFirst())
                    .isNotPresent();
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 파라미터로 들어오면, EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long courseId = 100L;
            Long userId = 1L;

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourse(courseId, 1L)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("해당 코스의 작성자가 아닌 유저의 식별값이 파라미터로 들어오면, CustomException 발생한다. 내부에 ErrorCode.NO_AUTHORITIES 가진다.")
        void notWriter() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourse(courseId, invalidUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }
}
