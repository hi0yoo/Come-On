package com.comeon.courseservice.domain.courselike.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
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

import javax.persistence.EntityManager;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
class CourseLikeServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseLikeRepository courseLikeRepository;

    @SpyBean
    CourseLikeService courseLikeService;

    @Nested
    @DisplayName("코스 좋아요 등록/삭제")
    class updateCourseLike {

        Long courseId;

        @BeforeEach
        void initCourseAndPlaces() {
            Course course = Course.builder()
                    .userId(1L)
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
                        .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                        .order(i)
                        .kakaoPlaceId((long) (i + 10000))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
            }

            courseId = courseRepository.save(course).getId();
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 없고, 코스가 작성 완료 상태라면, 해당 코스에 좋아요를 등록한다. 코스의 좋아요 count가 1 증가한다.")
        void courseLikeSaveSuccess() {
            courseRepository.findById(courseId).ifPresent(Course::updateCourseState);
            em.flush();
            em.clear();

            // given
            Long likeUserId = 10L;

            // when
            Long courseLikeId = courseLikeService.updateCourseLike(courseId, likeUserId);

            // then
            CourseLike courseLike = courseLikeRepository.findById(courseLikeId).orElseThrow();
            assertThat(courseLike.getId()).isEqualTo(courseLikeId);
            assertThat(courseLike.getUserId()).isEqualTo(likeUserId);
            assertThat(courseLike.getCourse()).isNotNull();
            assertThat(courseLike.getCourse().getLikeCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 없지만, 코스가 작성 완료상태가 아니라면, CustomException 발생한다. ErrorCode.CAN_NOT_ACCESS_RESOURSE")
        void courseLikeSaveFail() {
            // given
            Long likeUserId = 10L;

            // when, then
            assertThatThrownBy(
                    () -> courseLikeService.updateCourseLike(courseId, likeUserId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        @Test
        @DisplayName("특정 코스에 유저의 식별값으로 등록된 좋아요가 있으면, 코스의 좋아요 count를 1 감소시키고 해당 좋아요를 삭제한다.")
        void courseLikeRemove() {
            // given
            Long likeUserId = 3L;

            Course course = courseRepository.findById(courseId).orElseThrow();
            course.updateCourseState();

            courseLikeRepository.save(
                    CourseLike.builder()
                            .course(course)
                            .userId(likeUserId)
                            .build()
            );

            Integer likeCountBeforeLikeDelete = course.getLikeCount();

            em.flush();
            em.clear();

            // when
            Long courseLikeId = courseLikeService.updateCourseLike(courseId, likeUserId);

            // then
            assertThat(courseLikeId).isNull();
            assertThat(courseLikeRepository.findByCourseIdAndUserIdFetchCourse(courseId, likeUserId))
                    .isNotPresent();
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.getLikeCount()).isEqualTo(likeCountBeforeLikeDelete - 1);
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면 EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long invalidCourseId = 100L;
            Long userId = 10L;

            // when, then
            assertThatThrownBy(
                    () -> courseLikeService.updateCourseLike(invalidCourseId, userId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}