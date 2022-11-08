package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CoursePlaceDeleteResponse {

    private Long targetCourseId;
    private CourseStatus courseStatus;
    private List<CoursePlaceDetails> coursePlaces;

    public CoursePlaceDeleteResponse(Course course) {
        this.targetCourseId = course.getId();
        this.courseStatus = course.getCourseStatus();
        this.coursePlaces = course.getCoursePlaces().stream().map(CoursePlaceDetails::new).collect(Collectors.toList());
    }
}
