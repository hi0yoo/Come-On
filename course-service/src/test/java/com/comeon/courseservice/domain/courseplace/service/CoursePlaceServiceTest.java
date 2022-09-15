package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.AbstractServiceTest;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Slf4j
public class CoursePlaceServiceTest extends AbstractServiceTest {

    @Mock
    CourseRepository courseRepository;

    @InjectMocks
    CoursePlaceService coursePlaceService;

    @Nested
    @DisplayName("코스 장소 리스트 등록/수정/삭제")
    class batchUpdateCoursePlace {

        @Test
        @DisplayName("존재하는 코스 식별값, 해당 코스의 작성자 식별값, dto들이 들어오면, 장소 리스트 변경에 성공한다.")
        void success() {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();
            setCoursePlaces(course, 4);

            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName")
                            .description("placeDescription")
                            .lat(12.34)
                            .lng(23.45)
                            .order(1)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(1L)
                            .order(2)
                            .build()
            );
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(2L)
                            .order(3)
                            .build()
            );

            List<Long> coursePlaceIdsToDelete = new ArrayList<>();
            coursePlaceIdsToDelete.add(3L);
            coursePlaceIdsToDelete.add(4L);

            // mocking
            when(courseRepository.findByIdFetchCoursePlaces(courseId))
                    .thenReturn(Optional.of(course));

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, coursePlaceIdsToDelete);

            // then
            assertThat(course.isWritingComplete()).isTrue();
            assertThat(course.getCoursePlaces().size()).isEqualTo(dtosToSave.size() + dtosToModify.size());

            CoursePlace order2CoursePlace = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder().equals(2))
                    .findFirst().orElse(null);
            assertThat(order2CoursePlace).isNotNull();
            assertThat(order2CoursePlace.getId()).isEqualTo(1L);
            assertThat(order2CoursePlace.getName()).isNotNull();

            CoursePlace order3CoursePlace = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder().equals(3))
                    .findFirst().orElse(null);
            assertThat(order3CoursePlace).isNotNull();
            assertThat(order3CoursePlace.getId()).isEqualTo(2L);
            assertThat(order3CoursePlace.getName()).isNotNull();

            assertThat(course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getId() != null)
                    .anyMatch(coursePlace -> coursePlace.getId().equals(3L))).isFalse();
            assertThat(course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getId() != null)
                    .anyMatch(coursePlace -> coursePlace.getId().equals(4L))).isFalse();

            CoursePlace savedCoursePlace = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getId() == null)
                    .findFirst()
                    .orElse(null);
            CoursePlaceDto coursePlaceDto = dtosToSave.stream().findFirst().orElseThrow();
            assertThat(savedCoursePlace).isNotNull();
            assertThat(savedCoursePlace.getCourse()).isEqualTo(course);
            assertThat(savedCoursePlace.getName()).isEqualTo(coursePlaceDto.getName());
            assertThat(savedCoursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
            assertThat(savedCoursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
            assertThat(savedCoursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
            assertThat(savedCoursePlace.getOrder()).isEqualTo(coursePlaceDto.getOrder());
            assertThat(savedCoursePlace.getKakaoPlaceId()).isEqualTo(coursePlaceDto.getKakaoPlaceId());
            assertThat(savedCoursePlace.getPlaceCategory()).isEqualTo(coursePlaceDto.getPlaceCategory());
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면, EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long courseId = 100L;
            Long userId = 1L;

            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            List<Long> coursePlaceIdsToDelete = new ArrayList<>();

            // mocking
            when(courseRepository.findByIdFetchCoursePlaces(courseId))
                    .thenThrow(new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId));

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, coursePlaceIdsToDelete)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("해당 코스의 작성자가 아닌 유저의 식별값이 들어오면, CustomException 발생. ErrorCode.NO_AUTHORITIES")
        void notWriter() {
            // given
            Long userId = 1000L;
            Course course = setCourses(1L, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName")
                            .description("placeDescription")
                            .lat(12.34)
                            .lng(23.45)
                            .order(1)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            List<Long> coursePlaceIdsToDelete = new ArrayList<>();

            // mocking
            when(courseRepository.findByIdFetchCoursePlaces(courseId))
                    .thenReturn(Optional.of(course));

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, coursePlaceIdsToDelete)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }
}
