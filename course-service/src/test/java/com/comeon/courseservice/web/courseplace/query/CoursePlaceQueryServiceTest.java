package com.comeon.courseservice.web.courseplace.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CoursePlaceQueryServiceTest {

    @Mock
    CoursePlaceQueryRepository coursePlaceQueryRepository;

    @InjectMocks
    CoursePlaceQueryService coursePlaceQueryService;

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

    @Nested
    @DisplayName("코스 장소 리스트 조회")
    class getCoursePlaces {

        // 정상적으로 조회한 경우
        @Test
        @DisplayName("작성 완료된 코스의 식별자가 넘어와 정상적으로 조회한 경우, 조회에 성공한다.")
        void success() {
            //given
            initCourseAndPlaces();
            course.completeWriting();
            Long courseId = course.getId();

            given(coursePlaceQueryRepository.findCourseByCourseIdFetchPlaces(courseId))
                    .willReturn(Optional.of(course));

            //when
            ListResponse<CoursePlaceDetails> listResponse = coursePlaceQueryService.getCoursePlaces(courseId);

            //then
            assertThat(listResponse).isNotNull();
            assertThat(listResponse.getCount()).isEqualTo(course.getCoursePlaces().size());
            boolean containsAll = course.getCoursePlaces().stream()
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList())
                    .containsAll(
                            listResponse.getContents().stream()
                                    .map(CoursePlaceDetails::getCoursePlaceId)
                                    .collect(Collectors.toList())
                    );
            assertThat(containsAll).isTrue();
            listResponse.getContents()
                    .forEach(
                            coursePlaceDetails -> {
                                assertThat(coursePlaceDetails.getDescription()).isNotNull();
                                assertThat(coursePlaceDetails.getName()).isNotNull();
                                assertThat(coursePlaceDetails.getLat()).isNotNull();
                                assertThat(coursePlaceDetails.getLng()).isNotNull();
                                assertThat(coursePlaceDetails.getOrder()).isNotNull();
                            }
                    );
        }

        // 작성완료되지 않은 코스인 경우
        @Test
        @DisplayName("작성 완료이 완료되지 않은 코스의 경우, CustomException 발생, ErrorCode.CAN_NOT_ACCESS_RESOURCE 가진다.")
        void fail1() {
            //given
            initCourseAndPlaces();
            Long courseId = course.getId();

            given(coursePlaceQueryRepository.findCourseByCourseIdFetchPlaces(courseId))
                    .willReturn(Optional.of(course));

            //when, Then
            assertThatThrownBy(
                    () -> coursePlaceQueryService.getCoursePlaces(courseId)
            ).isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        // 코스가 존재하지 않는 경우
        @Test
        @DisplayName("해당 식별자를 갖는 코스가 없을 경우, EntityNotFoundException 발생한다.")
        void fail2() {
            //given
            Long courseId = 100L;

            given(coursePlaceQueryRepository.findCourseByCourseIdFetchPlaces(courseId))
                    .willReturn(Optional.empty());

            //when, Then
            assertThatThrownBy(
                    () -> coursePlaceQueryService.getCoursePlaces(courseId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}