package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
public class CoursePlaceServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    CourseRepository courseRepository;

    @SpyBean
    CoursePlaceService coursePlaceService;

    @Nested
    @DisplayName("코스 장소 리스트 등록/수정/삭제")
    class batchUpdateCoursePlace {

        Long userId = 1L;
        Long courseId;

        @BeforeEach
        void initCourse() {
            Course course = courseRepository.save(
                    Course.builder()
                            .userId(userId)
                            .title("courseTitle")
                            .description("courseDescription")
                            .courseImage(
                                    CourseImage.builder()
                                            .originalName("originalFileName")
                                            .storedName("storedFileName")
                                            .build()
                            )
                            .build()
            );
            courseId = course.getId();
        }

        @Test
        @DisplayName("코스 장소 리스트 등록에 성공한다. 코스 상태가 WRITING 이라면 COMPLETE로 변경한다.")
        void saveSuccess() {
            // given
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .order(1)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName2")
                            .description("placeDescription2")
                            .lat(34.56)
                            .lng(45.45)
                            .order(2)
                            .kakaoPlaceId(101L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, new ArrayList<>(), new ArrayList<>());
            em.flush();
            em.clear();

            // then
            Course course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.isWritingComplete()).isTrue();
            assertThat(course.getCoursePlaces().size()).isEqualTo(dtosToSave.size());

            course.getCoursePlaces()
                    .forEach(coursePlace -> {
                        assertThat(coursePlace.getId()).isNotNull();
                        assertThat(coursePlace.getCourse()).isEqualTo(course);
                        assertThat(coursePlace.getName()).isNotNull();
                        assertThat(coursePlace.getDescription()).isNotNull();
                        assertThat(coursePlace.getLat()).isNotNull();
                        assertThat(coursePlace.getLng()).isNotNull();
                        assertThat(coursePlace.getOrder()).isNotNull();
                        assertThat(coursePlace.getKakaoPlaceId()).isNotNull();
                        assertThat(coursePlace.getPlaceCategory()).isNotNull();
                    });
        }

        @Test
        @DisplayName("코스 장소 리스트 수정에 성공한다.")
        void modifySuccess() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            for (int i = 1; i <= 2; i++) {
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
            course.writeComplete();
            em.flush();
            em.clear();

            // given
            List<Long> coursePlaceIds = course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList());
            int coursePlaceCount = coursePlaceIds.size();

            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            for (int i = 0; i < coursePlaceCount; i++) {
                dtosToModify.add(
                        CoursePlaceDto.modifyBuilder()
                                .coursePlaceId(coursePlaceIds.get(i))
                                .name("modifyName" + i)
                                .description("modifyDescription" + i)
                                .lat(12.34 + i)
                                .lng(23.45 + i)
                                .order(i + 1)
                                .kakaoPlaceId((long) (100 + i))
                                .placeCategory(CoursePlaceCategory.CAFE)
                                .build()
                );
            }

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, new ArrayList<>(), dtosToModify, new ArrayList<>());
            em.flush();
            em.clear();

            // then
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.isWritingComplete()).isTrue();
            assertThat(course.getCoursePlaces().size()).isEqualTo(dtosToModify.size());

            course.getCoursePlaces()
                    .forEach(coursePlace -> {
                        CoursePlaceDto dto = dtosToModify.stream()
                                .filter(coursePlaceDto -> coursePlaceDto.getCoursePlaceId().equals(coursePlace.getId()))
                                .findFirst()
                                .orElseThrow();
                        assertThat(coursePlace.getName()).isEqualTo(dto.getName());
                        assertThat(coursePlace.getDescription()).isEqualTo(dto.getDescription());
                        assertThat(coursePlace.getLat()).isEqualTo(dto.getLat());
                        assertThat(coursePlace.getLng()).isEqualTo(dto.getLng());
                        assertThat(coursePlace.getOrder()).isEqualTo(dto.getOrder());
                        assertThat(coursePlace.getKakaoPlaceId()).isEqualTo(dto.getKakaoPlaceId());
                        assertThat(coursePlace.getPlaceCategory()).isEqualTo(dto.getPlaceCategory());
                    });
        }

        @Test
        @DisplayName("코스 장소 리스트 삭제에 성공한다.")
        void deleteSuccess() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            for (int i = 1; i <= 2; i++) {
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
            course.writeComplete();
            em.flush();
            em.clear();

            // given
            List<Long> coursePlaceIds = course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList());

            List<Long> placeIdsToDelete = new ArrayList<>();
            placeIdsToDelete.add(coursePlaceIds.stream().findFirst().orElseThrow());

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, new ArrayList<>(), new ArrayList<>(), placeIdsToDelete);
            em.flush();
            em.clear();

            // then
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.isWritingComplete()).isTrue();
            assertThat(course.getCoursePlaces().size()).isNotEqualTo(coursePlaceIds.size());
            assertThat(course.getCoursePlaces().size()).isEqualTo(coursePlaceIds.size() - placeIdsToDelete.size());
            assertThat(course.getCoursePlaces().stream().map(CoursePlace::getId).noneMatch(placeIdsToDelete::contains))
                    .isTrue();
        }

        @Test
        @DisplayName("코스 장소 리스트 등록/수정/삭제가 동시에 이루어지고, 성공한다.")
        void allSuccess() {
            Course course = courseRepository.findById(courseId).orElseThrow();
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
            course.writeComplete();
            em.flush();
            em.clear();

            // given
            int order = 1;
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .order(order++)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName2")
                            .description("placeDescription2")
                            .lat(34.56)
                            .lng(45.45)
                            .order(order++)
                            .kakaoPlaceId(101L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            List<Long> coursePlaceIds = course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList());
            int originalPlaceCount = coursePlaceIds.size();
            int deleteCount = 1;

            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            for (int i = 0; i < originalPlaceCount - deleteCount; i++) {
                dtosToModify.add(
                        CoursePlaceDto.modifyBuilder()
                                .coursePlaceId(coursePlaceIds.get(i))
                                .order(order++)
                                .placeCategory(CoursePlaceCategory.ACTIVITY)
                                .build()
                );
            }

            List<Long> placeIdsToDelete = new ArrayList<>();
            for (int i = originalPlaceCount - deleteCount; i < originalPlaceCount; i++) {
                placeIdsToDelete.add(coursePlaceIds.get(i));
            }

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, placeIdsToDelete);
            em.flush();
            em.clear();

            // then
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.getCoursePlaces().size()).isEqualTo(dtosToSave.size() + dtosToModify.size());
            for (CoursePlaceDto coursePlaceDto : dtosToSave) {
                course.getCoursePlaces().stream()
                        .filter(coursePlace -> coursePlace.getOrder().equals(coursePlaceDto.getOrder()))
                        .findFirst()
                        .ifPresent(coursePlace -> {
                            assertThat(coursePlace.getId()).isNotNull();
                            assertThat(coursePlace.getName()).isEqualTo(coursePlaceDto.getName());
                            assertThat(coursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
                            assertThat(coursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
                            assertThat(coursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
                            assertThat(coursePlace.getKakaoPlaceId()).isEqualTo(coursePlaceDto.getKakaoPlaceId());
                            assertThat(coursePlace.getPlaceCategory()).isEqualTo(coursePlaceDto.getPlaceCategory());
                        });
            }

            for (CoursePlaceDto coursePlaceDto : dtosToModify) {
                course.getCoursePlaces().stream()
                        .filter(coursePlace -> coursePlace.getId().equals(coursePlaceDto.getCoursePlaceId()))
                        .findFirst()
                        .ifPresent(coursePlace -> {
                            if (coursePlaceDto.getName() != null) {
                                assertThat(coursePlace.getName()).isEqualTo(coursePlaceDto.getName());
                            }
                            if (coursePlaceDto.getDescription() != null) {
                                assertThat(coursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
                            }
                            if (coursePlaceDto.getLat() != null) {
                                assertThat(coursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
                            }
                            if (coursePlaceDto.getLng() != null) {
                                assertThat(coursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
                            }
                            if (coursePlaceDto.getOrder() != null) {
                                assertThat(coursePlace.getOrder()).isEqualTo(coursePlaceDto.getOrder());
                            }
                            if (coursePlaceDto.getKakaoPlaceId() != null) {
                                assertThat(coursePlace.getKakaoPlaceId()).isEqualTo(coursePlaceDto.getKakaoPlaceId());
                            }
                            if (coursePlaceDto.getPlaceCategory() != null) {
                                assertThat(coursePlace.getPlaceCategory()).isEqualTo(coursePlaceDto.getPlaceCategory());
                            }
                        });
            }

            for (Long coursePlaceId : placeIdsToDelete) {
                assertThat(course.getCoursePlaces().stream()
                        .filter(coursePlace -> coursePlace.getId().equals(coursePlaceId))
                        .findFirst()).isNotPresent();
            }
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값이 들어오면, EntityNotFoundException 발생한다.")
        void entityNotFound() {
            // given
            Long invalidCourseId = 100L;
            Long userId = 1L;

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(invalidCourseId, userId, new ArrayList<>(), new ArrayList<>(), null)
            ).isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("해당 코스의 작성자가 아닌 유저의 식별값이 들어오면, CustomException 발생. ErrorCode.NO_AUTHORITIES")
        void notWriter() {
            // given
            Long invalidUserId = 1000L;

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, invalidUserId, new ArrayList<>(), new ArrayList<>(), null)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }
}
