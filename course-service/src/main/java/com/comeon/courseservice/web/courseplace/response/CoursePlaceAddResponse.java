package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoursePlaceAddResponse {

    private Long coursePlaceId;
    private Integer coursePlaceOrder;
    private CourseStatus courseStatus;
}
