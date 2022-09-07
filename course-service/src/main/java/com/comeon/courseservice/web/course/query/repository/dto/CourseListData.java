package com.comeon.courseservice.web.course.query.repository.dto;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Getter;

@Getter
public class CourseListData {

    private Course course;
    private CoursePlace coursePlace;
    private Double distance;
    private Long userLikeId;

    public CourseListData(Course course, CoursePlace coursePlace, Double distance, Long userLikeId) {
        this.course = course;
        this.coursePlace = coursePlace;
        this.distance = distance;
        this.userLikeId = userLikeId;
    }
}
