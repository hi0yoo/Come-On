package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
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

    @Autowired
    CoursePlaceRepository coursePlaceRepository;

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
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
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
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
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
                        .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                        .order(i)
                        .kakaoPlaceId((long) (i + 10000))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
            }
            course.updateCourseState();
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
                        .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                        .order(i)
                        .kakaoPlaceId((long) (i + 10000))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
            }
            course.updateCourseState();
            em.flush();
            em.clear();

            // given
            int originalCoursePlaceSize = course.getCoursePlaces().size();

            List<Long> placeIdsToDelete = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder() == 2)
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList());

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, new ArrayList<>(), new ArrayList<>(), placeIdsToDelete);
            em.flush();
            em.clear();

            // then
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.isWritingComplete()).isTrue();
            assertThat(course.getCoursePlaces().size()).isNotEqualTo(originalCoursePlaceSize);
            assertThat(course.getCoursePlaces().size()).isEqualTo(originalCoursePlaceSize - placeIdsToDelete.size());
            assertThat(course.getCoursePlaces().stream().map(CoursePlace::getId).noneMatch(placeIdsToDelete::contains))
                    .isTrue();
        }

        @Test
        @DisplayName("코스 장소 리스트 등록/수정/삭제가 동시에 이루어지고, 성공한다.")
        void allSuccess() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            // 코스에 5개의 장소 데이터 생성
            for (int i = 1; i <= 5; i++) {
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
            course.updateCourseState();
            em.flush();
            em.clear();

            // given
            int originalCoursePlaceSize = course.getCoursePlaces().size();

            // 삭제할 데이터(순서 2, 3번 장소 삭제)
            List<Long> placeIdsToDelete = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder() == 2 || coursePlace.getOrder() == 3)
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList());

            // 추가할 데이터(2번, 4번 순서로 생성)
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(2)
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
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(4)
                            .kakaoPlaceId(101L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            // 수정할 데이터(기존 4번 장소를 3번 순서 및 카테고리 수정)
            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(
                                    course.getCoursePlaces().stream()
                                            .filter(coursePlace -> coursePlace.getOrder() == 4)
                                            .findFirst()
                                            .map(CoursePlace::getId)
                                            .orElseThrow()
                            )
                            .order(3)
                            .placeCategory(CoursePlaceCategory.ACTIVITY)
                            .build()
            );

            // when
            coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, placeIdsToDelete);
            em.flush();
            em.clear();

            // then
            course = courseRepository.findById(courseId).orElseThrow();
            assertThat(course.getCoursePlaces().size())
                    .isEqualTo(dtosToSave.size() - placeIdsToDelete.size() + originalCoursePlaceSize);

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

        @Test
        @DisplayName("요청 데이터와 기존 데이터의 장소 순서가 중복되면 오류가 발생한다. ErrorCode.PLACE_ORDER_DUPLICATE")
        void placeOrderDuplicate() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            // 코스에 5개의 장소 데이터 생성
            for (int i = 1; i <= 5; i++) {
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
            course.updateCourseState();
            em.flush();
            em.clear();

            // given
            int originalCoursePlaceSize = course.getCoursePlaces().size();

            // 삭제할 데이터(순서 5번 장소 삭제)
            List<Long> placeIdsToDelete = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder() == 5)
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList());

            // 추가할 데이터(2번 순서로 생성)
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(2)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            // 수정할 데이터(기존 4번 장소의 카테고리 수정)
            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(
                                    course.getCoursePlaces().stream()
                                            .filter(coursePlace -> coursePlace.getOrder() == 4)
                                            .findFirst()
                                            .map(CoursePlace::getId)
                                            .orElseThrow()
                            )
                            .placeCategory(CoursePlaceCategory.ACTIVITY)
                            .build()
            );

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, placeIdsToDelete)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_ORDER_DUPLICATE);
        }

        @Test
        @DisplayName("요청 데이터를 처리한 장소 리스트에서 순서가 1부터 시작하지 않으면 오류가 발생한다. " +
                "ErrorCode.PLACE_ORDER_NOT_START_ONE")
        void placeOrderNotStartOne() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            // 코스에 5개의 장소 데이터 생성
            for (int i = 1; i <= 5; i++) {
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
            course.updateCourseState();
            em.flush();
            em.clear();

            // given
            int originalCoursePlaceSize = course.getCoursePlaces().size();

            // 삭제할 데이터(순서 1번 장소 삭제)
            List<Long> placeIdsToDelete = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder() == 1)
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList());

            // 추가할 데이터(6번 순서로 생성)
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(6)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            // 수정할 데이터(기존 4번 장소의 카테고리 수정)
            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(
                                    course.getCoursePlaces().stream()
                                            .filter(coursePlace -> coursePlace.getOrder() == 4)
                                            .findFirst()
                                            .map(CoursePlace::getId)
                                            .orElseThrow()
                            )
                            .placeCategory(CoursePlaceCategory.ACTIVITY)
                            .build()
            );

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, placeIdsToDelete)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_ORDER_NOT_START_ONE);
        }

        @Test
        @DisplayName("요청 데이터를 처리한 장소 리스트에서 순서가 연속적으로 증가하지 않으면 오류가 발생한다. " +
                "ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE")
        void placeOrderNotConsecutive() {
            Course course = courseRepository.findById(courseId).orElseThrow();
            // 코스에 5개의 장소 데이터 생성
            for (int i = 1; i <= 5; i++) {
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
            course.updateCourseState();
            em.flush();
            em.clear();

            // given
            // 삭제할 데이터(순서 3번 장소 삭제)
            List<Long> placeIdsToDelete = course.getCoursePlaces().stream()
                    .filter(coursePlace -> coursePlace.getOrder() == 3)
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList());

            // 추가할 데이터(6번 순서로 생성)
            List<CoursePlaceDto> dtosToSave = new ArrayList<>();
            dtosToSave.add(
                    CoursePlaceDto.builder()
                            .name("placeName1")
                            .description("placeDescription1")
                            .lat(12.34)
                            .lng(23.45)
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(6)
                            .kakaoPlaceId(100L)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build()
            );

            // 수정할 데이터(기존 4번 장소의 카테고리 수정)
            List<CoursePlaceDto> dtosToModify = new ArrayList<>();
            dtosToModify.add(
                    CoursePlaceDto.modifyBuilder()
                            .coursePlaceId(
                                    course.getCoursePlaces().stream()
                                            .filter(coursePlace -> coursePlace.getOrder() == 4)
                                            .findFirst()
                                            .map(CoursePlace::getId)
                                            .orElseThrow()
                            )
                            .placeCategory(CoursePlaceCategory.ACTIVITY)
                            .build()
            );

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchUpdateCoursePlace(courseId, userId, dtosToSave, dtosToModify, placeIdsToDelete)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE);
        }
    }

    @Nested
    @DisplayName("코스 장소 등록")
    class coursePlaceAdd {

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
        @DisplayName("코스의 작성자라면 코스 장소 등록에 성공하고 등록한 코스 장소의 식별값을 반환한다.")
        void success() {
            // given
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.builder()
                    .name("placeName")
                    .description("decription")
                    .lat(12.45)
                    .lng(34.56)
                    .address("address")
                    .kakaoPlaceId(12345L)
                    .placeCategory(CoursePlaceCategory.ETC)
                    .build();

            // when
            Long addCoursePlaceId = coursePlaceService.coursePlaceAdd(courseId, userId, coursePlaceDto);
            CoursePlace coursePlace = coursePlaceRepository.findById(addCoursePlaceId).orElseThrow();

            // then
            assertThat(coursePlace.getId()).isEqualTo(addCoursePlaceId);
            assertThat(coursePlace.getName()).isEqualTo(coursePlaceDto.getName());
            assertThat(coursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
            assertThat(coursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
            assertThat(coursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
            assertThat(coursePlace.getAddress()).isEqualTo(coursePlaceDto.getAddress());
            assertThat(coursePlace.getKakaoPlaceId()).isEqualTo(coursePlaceDto.getKakaoPlaceId());
            assertThat(coursePlace.getPlaceCategory()).isEqualTo(coursePlaceDto.getPlaceCategory());

            assertThat(coursePlace.getOrder()).isEqualTo(coursePlace.getCourse().getCoursePlaces().size());
        }

        @Test
        @DisplayName("코스의 작성자가 아니면 ErrorCode.NO_AUTHORITIES 에러코드를 가진 예외가 발생한다.")
        void notWriter() {
            // given
            Long invalidUserId = 100L;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.builder()
                    .name("placeName")
                    .description("decription")
                    .lat(12.45)
                    .lng(34.56)
                    .address("address")
                    .kakaoPlaceId(12345L)
                    .placeCategory(CoursePlaceCategory.ETC)
                    .build();

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceAdd(courseId, invalidUserId, coursePlaceDto)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }
    }

    @Nested
    @DisplayName("코스 장소 수정")
    class coursePlaceModify {

        Long userId = 1L;
        Long courseId;
        Long firstPlaceId;

        Integer size;

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

            for (int i = 1; i <= 5; i++) {
                CoursePlace coursePlace = CoursePlace.builder()
                        .course(course)
                        .name("placeName" + i)
                        .description("description" + i)
                        .order(i)
                        .lat(i + 12.34)
                        .lng(i + 34.56)
                        .address("address" + i)
                        .kakaoPlaceId((long) (i + 1234))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
                coursePlaceRepository.save(coursePlace);
                if (i == 1) {
                    firstPlaceId = coursePlace.getId();
                }
            }
            course.availableCourse();

            size = course.getCoursePlaces().size();
        }

        @Test
        @DisplayName("코스의 작성자라면 코스 장소 수정에 성공한다. 장소의 설명과 카테고리를 수정한다.")
        void successDescriptionAndCategory() {
            em.flush();
            em.clear();

            // given
            String description = "changeDescription";
            CoursePlaceCategory category = CoursePlaceCategory.SCHOOL;

            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .description(description)
                    .placeCategory(category)
                    .build();

            Long targetPlaceId = firstPlaceId;

            // when
            coursePlaceService.coursePlaceModify(courseId, userId, targetPlaceId, coursePlaceDto);
            CoursePlace coursePlace = coursePlaceRepository.findById(targetPlaceId).orElseThrow();

            // then
            assertThat(coursePlace.getDescription()).isEqualTo(description);
            assertThat(coursePlace.getPlaceCategory()).isEqualTo(category);
        }

        @Test
        @DisplayName("코스의 작성자라면 코스 장소 수정에 성공한다. 장소의 설명과 카테고리가 null로 들어오면 수정하지 않는다.")
        void descriptionAndCategoryIsOptional() {
            em.flush();
            em.clear();

            // given
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .build();

            Long targetPlaceId = firstPlaceId;

            CoursePlace originalCoursePlace = coursePlaceRepository.findById(targetPlaceId).orElseThrow();

            // when
            coursePlaceService.coursePlaceModify(courseId, userId, targetPlaceId, coursePlaceDto);
            CoursePlace coursePlace = coursePlaceRepository.findById(targetPlaceId).orElseThrow();

            // then
            assertThat(coursePlace.getDescription()).isEqualTo(originalCoursePlace.getDescription());
            assertThat(coursePlace.getPlaceCategory()).isEqualTo(originalCoursePlace.getPlaceCategory());
        }

        @Test
        @DisplayName("코스의 작성자라면 코스 장소 수정에 성공한다. 순서가 들어오면 해당 순서의 장소와 순서를 swap 한다.")
        void successSwapOrder() {
            em.flush();
            em.clear();

            // given
            Integer targetOrder = 4;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .order(targetOrder)
                    .build();

            Long originalPlaceId = firstPlaceId;

            List<CoursePlace> coursePlaces = coursePlaceRepository.findAllByCourseId(courseId);
            CoursePlace firstPlace = coursePlaces.stream().filter(coursePlace -> coursePlace.getId().equals(firstPlaceId))
                    .findFirst()
                    .orElseThrow();
            Integer firstPlaceOrder = firstPlace.getOrder();
            CoursePlace targetPlace = coursePlaces.stream().filter(coursePlace -> coursePlace.getOrder().equals(targetOrder))
                    .findFirst()
                    .orElseThrow();
            Integer targetPlaceOrder = targetPlace.getOrder();

            // when
            coursePlaceService.coursePlaceModify(courseId, userId, originalPlaceId, coursePlaceDto);
            CoursePlace coursePlace = coursePlaceRepository.findById(originalPlaceId).orElseThrow();

            // then
            assertThat(coursePlace.getOrder()).isNotEqualTo(firstPlaceOrder);
            assertThat(coursePlace.getOrder()).isEqualTo(targetPlaceOrder);
            assertThat(targetPlace.getOrder()).isNotEqualTo(targetPlaceOrder);
            assertThat(targetPlace.getOrder()).isEqualTo(firstPlaceOrder);
        }

        @Test
        @DisplayName("순서를 변경할 때 dto로 넘어온 순서가 장소 리스트의 개수보다 크면, ErrorCode.NOT_EXIST_PLACE_ORDER를 가진 예외가 발생한다.")
        void notExistOrder() {
            em.flush();
            em.clear();

            // given
            int targetOrder = size + 1;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .order(targetOrder)
                    .build();

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceModify(courseId, userId, firstPlaceId, coursePlaceDto)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_EXIST_PLACE_ORDER);
        }

        @Test
        @DisplayName("코스의 작성자가 아니면 ErrorCode.NO_AUTHORITIES 에러코드를 가진 예외가 발생한다.")
        void notWriter() {
            em.flush();
            em.clear();

            // given
            Long invalidUserId = 100L;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .build();

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceModify(courseId, invalidUserId, firstPlaceId, coursePlaceDto)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("지정한 코스 장소가 없다면 EntityNotFoundException 발생한다.")
        void coursePlaceNotFound() {
            em.flush();
            em.clear();

            // given
            Long invalidCoursePlaceId = 1000L;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.modifyBuilder()
                    .build();

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceModify(courseId, userId, invalidCoursePlaceId, coursePlaceDto)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("코스 장소 삭제")
    class coursePlaceRemove {

        Long userId = 1L;
        Long courseId;
        Long firstPlaceId;

        Integer size;

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

            for (int i = 1; i <= 5; i++) {
                CoursePlace coursePlace = CoursePlace.builder()
                        .course(course)
                        .name("placeName" + i)
                        .description("description" + i)
                        .order(i)
                        .lat(i + 12.34)
                        .lng(i + 34.56)
                        .address("address" + i)
                        .kakaoPlaceId((long) (i + 1234))
                        .placeCategory(CoursePlaceCategory.ETC)
                        .build();
                coursePlaceRepository.save(coursePlace);
                if (i == 1) {
                    firstPlaceId = coursePlace.getId();
                }
            }
            course.availableCourse();

            size = course.getCoursePlaces().size();
        }

        @Test
        @DisplayName("코스의 작성자라면 코스 장소 삭제에 성공한다. 삭제한 장소의 순서 이후의 장소들은 순서가 1씩 앞당겨진다.")
        void success() {
            // given
            List<CoursePlace> originalPlaces = coursePlaceRepository.findAllByCourseId(courseId);
            em.flush();
            em.clear();

            // when
            coursePlaceService.coursePlaceRemove(courseId, userId, firstPlaceId);
            List<CoursePlace> coursePlaces = coursePlaceRepository.findAllByCourseId(courseId)
                    .stream()
                    .sorted(Comparator.comparing(CoursePlace::getOrder))
                    .collect(Collectors.toList());

            // then
            assertThat(coursePlaces.stream()
                    .filter(coursePlace -> coursePlace.getId().equals(firstPlaceId))
                    .findFirst()).isEmpty();
            assertThat(coursePlaces.size()).isEqualTo(size - 1);

            List<CoursePlace> originalWithoutRemoved = originalPlaces.stream()
                    .filter(coursePlace -> !Objects.equals(coursePlace.getId(), firstPlaceId))
                    .sorted(Comparator.comparing(CoursePlace::getOrder))
                    .collect(Collectors.toList());

            for (CoursePlace coursePlace : coursePlaces) {
                assertThat(originalWithoutRemoved.stream()
                        .filter(cp -> cp.getId().equals(coursePlace.getId()))
                        .findFirst()
                        .orElseThrow()
                        .getOrder() - 1
                ).isEqualTo(coursePlace.getOrder());
            }
        }

        @Test
        @DisplayName("코스의 작성자라면 코스 장소 삭제에 성공한다. 장소가 0이 되면 코스는 DISABLED 상태로 변경된다.")
        void disabledCourse() {
            // given
            em.flush();
            em.clear();
            List<CoursePlace> originalPlaces = coursePlaceRepository.findAllByCourseId(courseId);

            // when
            for (CoursePlace originalPlace : originalPlaces) {
                System.out.println(1);
                coursePlaceService.coursePlaceRemove(courseId, userId, originalPlace.getId());
            }

            em.flush();
            em.clear();

            Course course = courseRepository.findById(courseId).orElseThrow();

            // then
            assertThat(course.getCoursePlaces().size()).isEqualTo(0);
            assertThat(course.getCourseStatus()).isEqualTo(CourseStatus.DISABLED);
        }

        @Test
        @DisplayName("코스의 작성자가 아니면 ErrorCode.NO_AUTHORITIES 에러코드를 가진 예외가 발생한다.")
        void notWriter() {
            // given
            Long invalidUserId = 100L;

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceRemove(courseId, invalidUserId, firstPlaceId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("지정한 코스 장소가 없다면 EntityNotFoundException 발생한다.")
        void coursePlaceNotFound() {
            // given
            Long invalidCoursePlaceId = 1000L;

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.coursePlaceRemove(courseId, userId, invalidCoursePlaceId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}
