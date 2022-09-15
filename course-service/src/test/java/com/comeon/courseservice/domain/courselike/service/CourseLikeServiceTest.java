package com.comeon.courseservice.domain.courselike.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.AbstractServiceTest;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
class CourseLikeServiceTest extends AbstractServiceTest {

    @Mock
    CourseRepository courseRepository;

    @Mock
    CourseLikeRepository courseLikeRepository;

    @InjectMocks
    CourseLikeService courseLikeService;

    @Nested
    @DisplayName("코스 좋아요 등록/삭제")
    class updateCourseLike {

        private CourseLike generateCourseLike(Course course, Long likeUserId) {
            CourseLike courseLike = CourseLike.builder()
                    .course(course)
                    .userId(likeUserId)
                    .build();
            // 테스트에서 1번, 실제 서비스 로직에서 1번 생성되기 때문에 count -1 처리
            course.decreaseLikeCount();
            ReflectionTestUtils.setField(courseLike, "id", 1L);
            return courseLike;
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 없고, 코스가 작성 완료 상태라면, 해당 코스에 좋아요를 등록한다. 코스의 좋아요 count가 1 증가한다.")
        void courseLikeSaveSuccess() {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 2);
            Long courseId = course.getId();

            Integer countBeforeSaveLike = course.getLikeCount();

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            Long likeUserId = 3L;
            when(courseLikeRepository.findByCourseIdAndUserIdFetchCourse(courseId, likeUserId))
                    .thenReturn(Optional.empty());

            when(courseLikeRepository.save(any(CourseLike.class)))
                    .thenReturn(generateCourseLike(course, likeUserId));

            // when
            Long courseLikeId = courseLikeService.updateCourseLike(courseId, likeUserId);

            // then
            assertThat(courseLikeId).isNotNull();
            assertThat(course.getLikeCount()).isEqualTo(countBeforeSaveLike + 1);
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 없지만, 코스가 작성 완료상태가 아니라면, CustomException 발생한다. ErrorCode.CAN_NOT_ACCESS_RESOURSE")
        void courseLikeSaveFail() {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            Long likeUserId = 3L;
            when(courseLikeRepository.findByCourseIdAndUserIdFetchCourse(courseId, likeUserId))
                    .thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(
                    () -> courseLikeService.updateCourseLike(courseId, likeUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 있으면, 코스의 좋아요 count를 1 감소시키고 해당 좋아요를 삭제한다.")
        void courseLikeRemove() {
            // given
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 2);
            Long courseId = course.getId();
            Long likeUserId = 3L;
            setCourseLike(course, likeUserId);
            CourseLike courseLike = getCourseLikeList().stream().findFirst().orElseThrow();

            Integer countBeforeSaveLike = course.getLikeCount();

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.of(course));

            when(courseLikeRepository.findByCourseIdAndUserIdFetchCourse(courseId, likeUserId))
                    .thenReturn(Optional.of(courseLike));

            doAnswer(invocation -> {
                getCourseLikeList().remove(courseLike);
                return null;
            }).when(courseLikeRepository).delete(courseLike);

            // when
            Long courseLikeId = courseLikeService.updateCourseLike(courseId, likeUserId);

            // then
            assertThat(courseLikeId).isNull();
            assertThat(course.getLikeCount()).isEqualTo(countBeforeSaveLike - 1);
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면 EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long courseId = 100L;

            // mocking
            when(courseRepository.findById(courseId))
                    .thenReturn(Optional.empty());

            // when, then
            assertThatThrownBy(
                    () -> courseLikeService.updateCourseLike(courseId, anyLong())
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}