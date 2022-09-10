package com.comeon.courseservice.web.courseplace.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoursePlaceQueryService {

    private final CoursePlaceQueryRepository coursePlaceQueryRepository;

    public ListResponse<CoursePlaceDetails> getCoursePlaces(Long courseId) {
        Course course = coursePlaceQueryRepository.findCourseByCourseIdFetchPlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

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
}
