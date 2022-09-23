package com.comeon.meetingservice.web.common.feign.courseservice;

import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseListResponse;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.courseservice.response.CourseServiceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseServiceFeignClient {

    @GetMapping("/courses/{courseId}/course-places")
    CourseServiceApiResponse<CourseServiceListResponse<CourseListResponse>> getCoursePlaces(
            @PathVariable("courseId") Long courseId);

}
