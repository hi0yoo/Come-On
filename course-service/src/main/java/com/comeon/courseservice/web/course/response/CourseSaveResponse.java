package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseSaveResponse {

    private Long courseId;
    private CourseStatus courseStatus;
}
