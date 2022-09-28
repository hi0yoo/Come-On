package com.comeon.courseservice.web.courseplace.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.AbstractQueryServiceTest;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class CoursePlaceQueryServiceTest extends AbstractQueryServiceTest {

    @Autowired
    CoursePlaceQueryRepository coursePlaceQueryRepository;

    @SpyBean
    CoursePlaceQueryService coursePlaceQueryService;

    @Nested
    @DisplayName("코스 장소 리스트 조회")
    class getCoursePlaces {

        // 정상적으로 조회한 경우
        @Test
        @DisplayName("작성 완료된 코스의 식별자가 넘어와 정상적으로 조회한 경우, 조회에 성공한다.")
        void success() {
            //given
            Long courseId = 1L;

            //when
            ListResponse<CoursePlaceDetails> listResponse = coursePlaceQueryService.getCoursePlaces(courseId);

            //then
            assertThat(listResponse).isNotNull();

            Course course = courseRepository.findById(courseId).orElseThrow();
            assertThat(listResponse.getCount()).isEqualTo(course.getCoursePlaces().size());
            boolean containsAll = course.getCoursePlaces().stream()
                    .map(CoursePlace::getId)
                    .collect(Collectors.toList())
                    .containsAll(
                            listResponse.getContents().stream()
                                    .map(CoursePlaceDetails::getId)
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
            Course course = Course.builder()
                    .userId(1L)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("original")
                                    .storedName("stored")
                                    .build()
                    )
                    .title("title")
                    .description("description")
                    .build();
            courseRepository.save(course);

            Long courseId = course.getId();

            //when, Then
            assertThatThrownBy(
                    () -> coursePlaceQueryService.getCoursePlaces(courseId)
            ).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        // 코스가 존재하지 않는 경우
        @Test
        @DisplayName("해당 식별자를 갖는 코스가 없을 경우, EntityNotFoundException 발생한다.")
        void fail2() {
            //given
            Long courseId = 100L;

            //when, Then
            assertThatThrownBy(
                    () -> coursePlaceQueryService.getCoursePlaces(courseId)
            ).isInstanceOf(EntityNotFoundException.class);
        }
    }
}