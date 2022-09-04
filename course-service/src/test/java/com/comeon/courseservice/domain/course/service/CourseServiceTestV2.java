package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseLike;
import com.comeon.courseservice.domain.course.repository.CourseLikeRepository;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CourseServiceTestV2 {

    @Mock
    CourseRepository courseRepository;

    @Mock
    CourseLikeRepository courseLikeRepository;

    @InjectMocks
    CourseService courseService;

    Course course;

    void initCourseAndPlaces() {
        course = Course.builder()
                .userId(1L)
                .title("courseTitle")
                .description("courseDescription")
                .courseImage(
                        CourseImage.builder()
                                .originalName("originalFileName")
                                .storedName("storedFileName")
                                .build()
                )
                .build();
        ReflectionTestUtils.setField(course, "id", 1L);

        int count = 5;
        String placeName = "placeName";
        String placeDescription = "placeDescription";
        Double placeLat = 12.34;
        Double placeLng = 23.45;
        List<CoursePlace> coursePlaceList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name(placeName + i)
                    .description(placeDescription + i)
                    .lat(placeLat + i)
                    .lng(placeLng + i)
                    .order(i)
                    .build();
            ReflectionTestUtils.setField(coursePlace, "id", (long) i);
            coursePlaceList.add(coursePlace);
        }
    }

    List<CourseLike> courseLikes = new ArrayList<>();

    void initCourseAndPlacesAndLike() {
        initCourseAndPlaces();

        for (int i = 1; i <= 50; i++) {
            CourseLike courseLike = CourseLike.builder()
                    .course(course)
                    .userId((long) i)
                    .build();
            ReflectionTestUtils.setField(courseLike, "id", (long) i);
            courseLikes.add(courseLike);
        }
    }

    @Nested
    @DisplayName("코스 좋아요 등록")
    class saveCourseLike {

        private CourseLike createCourseLike() {
            CourseLike courseLike = CourseLike.builder()
                    .course(course)
                    .build();
            ReflectionTestUtils.setField(courseLike, "id", 1L);
            return courseLike;
        }

        @Test
        @DisplayName("해당 코스에 좋아요를 등록하지 않았으면 새로 등록된다.")
        void success() {
            // given
            initCourseAndPlaces();
            when(courseRepository.findById(anyLong()))
                    .thenReturn(Optional.of(course));
            when(courseLikeRepository.findByCourseAndUserIdFetchCourse(any(), anyLong()))
                    .thenReturn(Optional.empty());
            when(courseLikeRepository.save(any()))
                    .thenReturn(createCourseLike());

            Long courseId = course.getId();
            Long userId = 1L;

            // when
            Long saveCourseLikeId = courseService.saveCourseLike(courseId, userId);

            // then
            assertThat(saveCourseLikeId).isNotNull();
        }

        @Test
        @DisplayName("해당 코스에 좋아요를 이미 등록했으면 CustomException 발생한다. ErrorCode.ALREADY_EXIST")
        void alreadyExist() {
            // given
            initCourseAndPlacesAndLike();
            when(courseRepository.findById(anyLong()))
                    .thenReturn(Optional.of(course));
            Long userId = 1L;
            when(courseLikeRepository.findByCourseAndUserIdFetchCourse(any(), anyLong()))
                    .thenReturn(courseLikes.stream()
                            .filter(
                                    courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(userId)
                            )
                            .findFirst()
                    );

            Long courseId = course.getId();

            // when, then
            assertThatThrownBy(
                    () -> courseService.saveCourseLike(courseId, userId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_EXIST);
        }

        @Test
        @DisplayName("코스 식별자와 일치하는 코스가 없다면 EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            when(courseRepository.findById(anyLong()))
                    .thenThrow(new EntityNotFoundException());

            Long courseId = 10L;
            Long userId = 10L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.saveCourseLike(courseId, userId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("코스 좋아요 삭제")
    class removeCourseLike {

        @Test
        @DisplayName("존재하지 않는 좋아요의 식별자를 넘긴 경우, EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            initCourseAndPlaces();
            when(courseLikeRepository.findByIdFetch(anyLong()))
                    .thenThrow(new EntityNotFoundException());

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourseLike(1L, 1L, 1L)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("좋아요가 등록된 코스와, 넘어온 courseId가 다른 경우, CustomException 발생. ErrorCode.VALIDATION_FAIL")
        void notSameCourseId() {
            // given
            initCourseAndPlacesAndLike();
            Long courseLikeId = 1L;
            when(courseLikeRepository.findByIdFetch(anyLong()))
                    .thenReturn(courseLikes.stream().filter(
                                    courseLike -> courseLike.getId().equals(courseLikeId)
                            ).findFirst()
                    );

            Long invalidCourseId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourseLike(courseLikeId, invalidCourseId, 1L)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAIL);
        }

        @Test
        @DisplayName("좋아요를 등록한 유저와 일치하지 않는 유저의 식별자가 넘어온 경우, CustomException 발생. ErrorCode.NO_AUTHORITIES")
        void noAuthorities() {
            // given
            initCourseAndPlacesAndLike();
            Long courseLikeId = 1L;
            CourseLike like = courseLikes.stream().filter(
                            courseLike -> courseLike.getId().equals(courseLikeId)
                    ).findFirst()
                    .orElse(null);
            when(courseLikeRepository.findByIdFetch(anyLong()))
                    .thenReturn(Optional.ofNullable(like));

            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> courseService.removeCourseLike(courseLikeId, like.getCourse().getId(), invalidUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("courseLikeId, courseId, userId 모두 일치하면 성공한다.")
        void success() {
            // given
            initCourseAndPlacesAndLike();
            CourseLike courseLike = courseLikes.stream()
                    .filter(
                            cl -> cl.getId().equals(1L) && cl.getCourse().getId().equals(1L) && cl.getUserId().equals(1L)
                    ).findFirst().orElse(null);
            assertThat(courseLike).isNotNull();

            when(courseLikeRepository.findByIdFetch(anyLong()))
                    .thenReturn(Optional.of(courseLike));
            doAnswer(invocation -> {
                return courseLikes.remove(courseLike);
            }).when(courseLikeRepository).delete(any());

            // when
            courseService.removeCourseLike(1L, 1L, 1L);

            // then
            CourseLike afterRemoveCourseLike = courseLikes.stream()
                    .filter(
                            cl -> cl.getId().equals(1L) && cl.getCourse().getId().equals(1L) && cl.getUserId().equals(1L)
                    ).findFirst().orElse(null);

            assertThat(afterRemoveCourseLike).isNull();
        }
    }
}
