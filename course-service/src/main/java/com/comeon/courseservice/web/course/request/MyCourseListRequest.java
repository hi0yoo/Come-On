package com.comeon.courseservice.web.course.request;

import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import com.comeon.courseservice.web.course.query.repository.cond.MyCourseCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseListRequest {

    @ValidEnum(enumClass = CourseStatus.class, ignoreCase = true)
    private String courseStatus;

    public MyCourseCondition toCondition() {
        return new MyCourseCondition(convertCourseStatusAndGet());
    }

    public CourseStatus convertCourseStatusAndGet() {
        return CourseStatus.of(courseStatus);
    }
}
