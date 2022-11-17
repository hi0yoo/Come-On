package com.comeon.courseservice.web.courseplace.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceAddResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDeleteResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceModifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoursePlaceQueryService {

    private final CoursePlaceQueryRepository coursePlaceQueryRepository;

    public CoursePlaceAddResponse getCoursePlaceAddResponse(Long courseId, Long addedCoursePlaceId) {
        Course course = getCourse(courseId);

        return new CoursePlaceAddResponse(course, addedCoursePlaceId);
    }

    public CoursePlaceModifyResponse getCoursePlaceModifyResponse(Long courseId) {
        return new CoursePlaceModifyResponse(getCourse(courseId));
    }

    public CoursePlaceDeleteResponse getCoursePlaceDeleteResponse(Long courseId) {
        return new CoursePlaceDeleteResponse(getCourse(courseId));
    }

    public ListResponse<CoursePlaceDetails> getCoursePlaceListResponse(Long courseId) {
        Course course = getCourse(courseId);

        // 작성 완료되지 않은 코스는 조회 X
        if (!course.isWritingComplete()) {
            throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.CAN_NOT_ACCESS_RESOURCE);
        }

        return ListResponse.toListResponse(
                course.getCoursePlaces().stream()
                        .map(CoursePlaceDetails::new)
                        .collect(Collectors.toList())
        );
    }

    public List<Long> getCoursePlaceIds(Long courseId) {
        return getCourse(courseId).getCoursePlaces().stream()
                .map(CoursePlace::getId)
                .collect(Collectors.toList());
    }

    public Integer getCoursePlaceOrder(Long coursePlaceId) {
        return coursePlaceQueryRepository.findById(coursePlaceId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 코스 장소가 존재하지 않습니다. 조회한 코스 장소 식별값 : " + coursePlaceId)
                ).getOrder();
    }


    /* ==== private method ==== */
    private Course getCourse(Long courseId) {
        Course course = coursePlaceQueryRepository.findCourseByCourseIdFetchPlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );
        return course;
    }
}
