package com.comeon.courseservice.web.course.query.repository.cond;

import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.Getter;

@Getter
public class MyCourseCondition {

    private CourseStatus courseStatus;

    public MyCourseCondition(CourseStatus courseStatus) {
        this.courseStatus = courseStatus;
    }
}
