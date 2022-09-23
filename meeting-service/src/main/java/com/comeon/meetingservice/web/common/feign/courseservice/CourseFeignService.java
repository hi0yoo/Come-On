package com.comeon.meetingservice.web.common.feign.courseservice;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseListResponse;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseServiceListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseFeignService {

    private final CourseServiceFeignClient courseServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public List<CourseListResponse> getCoursePlaceList(Long courseId) {
        CircuitBreaker courseListCb = circuitBreakerFactory.create("courseList");
        CourseServiceApiResponse<CourseServiceListResponse<CourseListResponse>> placeResponse
                = courseListCb.run(() -> courseServiceFeignClient.getCoursePlaces(courseId),
                throwable -> {
                    if (throwable instanceof CustomException) {
                        throw (CustomException) throwable;
                    } else {
                        throw new CustomException(throwable.getMessage(), ErrorCode.COURSE_SERVICE_ERROR);
                    }
                });

        return placeResponse.getData().getContents();
    }
}
