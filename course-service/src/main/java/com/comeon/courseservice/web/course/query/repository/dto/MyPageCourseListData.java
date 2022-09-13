package com.comeon.courseservice.web.course.query.repository.dto;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Getter;

@Getter
public class MyPageCourseListData {

    private Course course;
    private Long userLikeId;

    public MyPageCourseListData(Course course, Long userLikeId) {
        this.course = course;
        this.userLikeId = userLikeId;
    }
}
