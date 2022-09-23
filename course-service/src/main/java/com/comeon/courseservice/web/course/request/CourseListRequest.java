package com.comeon.courseservice.web.course.request;

import com.comeon.courseservice.web.course.query.repository.cond.CourseCondition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseListRequest {

    private String title;
    private Double lat;
    private Double lng;

    public CourseCondition toCondition() {
        return new CourseCondition(title, lat, lng);
    }
}
