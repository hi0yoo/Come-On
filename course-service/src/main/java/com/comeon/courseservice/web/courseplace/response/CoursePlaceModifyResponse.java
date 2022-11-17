package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CoursePlaceModifyResponse {

    private Long targetCourseId;
    private List<CoursePlaceDetails> coursePlaces;

    public CoursePlaceModifyResponse(Course course) {
        this.targetCourseId = course.getId();
        this.coursePlaces = course.getCoursePlaces().stream().map(CoursePlaceDetails::new).collect(Collectors.toList());
    }
}
