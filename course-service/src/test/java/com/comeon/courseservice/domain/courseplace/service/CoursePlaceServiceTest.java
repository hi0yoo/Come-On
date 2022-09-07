package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.entity.CourseWriteStatus;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CoursePlaceServiceTest {

    @Autowired
    CoursePlaceService coursePlaceService;

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
    @DisplayName("코스 장소 등록")
    class saveCoursePlace {

        @Test
        @DisplayName("코스를 등록한 유저가 장소를 등록하면, 코스 장소 등록에 성공한다.")
        void success() {
            // given
            initCourse();
            Long userId = course.getUserId();
            Long courseId = course.getId();

            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.builder()
                    .name(placeName)
                    .description(placeDescription)
                    .lat(placeLat)
                    .lng(placeLng)
                    .mapPlaceId(123345L)
                    .build();

            // when
            Long coursePlaceId = coursePlaceService.saveCoursePlace(courseId, userId, coursePlaceDto);

            // then
            CoursePlace coursePlace = coursePlaceRepository.findById(coursePlaceId).orElse(null);
            assertThat(coursePlace).isNotNull();
            assertThat(coursePlace.getCourse()).isEqualTo(course);
            assertThat(coursePlace.getName()).isEqualTo(coursePlaceDto.getName());
            assertThat(coursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
            assertThat(coursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
            assertThat(coursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
            assertThat(coursePlace.getOrder()).isNotNull();
            if (coursePlace.getCourse().getCoursePlaces().size() == 1) {
                assertThat(coursePlace.getOrder()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 코스를 등록한 유저와 다르면, ErrorCode.NO_AUTHORITIES 를 갖는 CustomException 발생한다.")
        void fail() {
            // given
            initCourse();
            Long invalidUserId = 100L;
            Long courseId = course.getId();

            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.builder()
                    .name(placeName)
                    .description(placeDescription)
                    .lat(placeLat)
                    .lng(placeLng)
                    .build();

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.saveCoursePlace(courseId, invalidUserId, coursePlaceDto)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("파라미터로 넘어온 courseId와 일치하는 코스 식별자가 없으면, EntityNotFoundException 예외를 발생시킨다.")
        void fail_2() {
            // given
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            CoursePlaceDto coursePlaceDto = CoursePlaceDto.builder()
                    .name(placeName)
                    .description(placeDescription)
                    .lat(placeLat)
                    .lng(placeLng)
                    .build();
            Long invalidCourseId = 100L;
            Long userId = 1L;

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.saveCoursePlace(invalidCourseId, userId, coursePlaceDto)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("코스 장소 리스트 등록")
    class batchSaveCoursePlace {

        @Test
        @DisplayName("코스를 등록한 유저가 장소 리스트를 등록하면, 코스 장소 리스트 등록에 성공하고, 코스의 작성 상태를 COMPLETE로 변경한다.")
        void success() {
            // given
            initCourse();
            Long userId = course.getUserId();
            Long courseId = course.getId();

            int count = 5;

            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlaceDto> coursePlaceDtoList = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                coursePlaceDtoList.add(
                        CoursePlaceDto.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .mapPlaceId((long) i)
                                .build()
                );
            }

            // when
            coursePlaceService.batchSaveCoursePlace(courseId, userId, coursePlaceDtoList);
            em.flush();
            em.clear();

            // then
            Course findCourse = courseRepository.findById(courseId).orElseThrow();
            assertThat(findCourse.getWriteStatus()).isEqualTo(CourseWriteStatus.COMPLETE);
            List<CoursePlace> coursePlaces = findCourse.getCoursePlaces();
            assertThat(coursePlaces.size()).isEqualTo(coursePlaceDtoList.size());
            for (CoursePlaceDto coursePlaceDto : coursePlaceDtoList) {
                CoursePlace matchCoursePlace = coursePlaces.stream()
                        .filter(coursePlace -> coursePlace.getOrder().equals(coursePlaceDto.getOrder()))
                        .findFirst()
                        .orElse(null);

                assertThat(matchCoursePlace).isNotNull();
                assertThat(matchCoursePlace.getCourse()).isEqualTo(findCourse);
                assertThat(matchCoursePlace.getName()).isEqualTo(coursePlaceDto.getName());
                assertThat(matchCoursePlace.getDescription()).isEqualTo(coursePlaceDto.getDescription());
                assertThat(matchCoursePlace.getLat()).isEqualTo(coursePlaceDto.getLat());
                assertThat(matchCoursePlace.getLng()).isEqualTo(coursePlaceDto.getLng());
            }
        }

        @Test
        @DisplayName("파라미터로 넘어온 userId가 코스를 등록한 유저와 다르면, ErrorCode.NO_AUTHORITIES 를 갖는 CustomException 발생한다.")
        void fail() {
            // given
            initCourse();
            Long invalidUserId = 100L;
            Long courseId = course.getId();

            int count = 5;

            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlaceDto> coursePlaceDtoList = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                coursePlaceDtoList.add(
                        CoursePlaceDto.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .mapPlaceId((long) i)
                                .build()
                );
            }

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchSaveCoursePlace(courseId, invalidUserId, coursePlaceDtoList)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITIES);
        }

        @Test
        @DisplayName("파라미터로 넘어온 courseId와 일치하는 코스 식별자가 없으면, EntityNotFoundException 예외를 발생시킨다.")
        void fail_2() {
            // given
            Long invalidCourseId = 100L;
            Long userId = 1L;

            int count = 5;

            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 34.56;
            List<CoursePlaceDto> coursePlaceDtoList = new ArrayList<>();

            for (int i = 1; i <= count; i++) {
                coursePlaceDtoList.add(
                        CoursePlaceDto.builder()
                                .name(placeName + i)
                                .description(placeDescription + i)
                                .lat(placeLat + i)
                                .lng(placeLng + i)
                                .order(i)
                                .mapPlaceId((long) i)
                                .build()
                );
            }

            // when, then
            assertThatThrownBy(
                    () -> coursePlaceService.batchSaveCoursePlace(invalidCourseId, userId, coursePlaceDtoList)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}