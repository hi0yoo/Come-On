package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.CourseStatus;

public class CoursePlaceDeleteResponse {

    private static final String SUCCESS_MESSAGE = "해당 코스 장소가 성공적으로 삭제되었습니다.";

    private String message;
    private CourseStatus courseStatus;

    public CoursePlaceDeleteResponse(String message, CourseStatus courseStatus) {
        this.message = message;
        this.courseStatus = courseStatus;
    }

    public CoursePlaceDeleteResponse(CourseStatus courseStatus) {
        this.message = SUCCESS_MESSAGE;
        this.courseStatus = courseStatus;
    }

    public String getMessage() {
        return message;
    }

    public CourseStatus getCourseStatus() {
        return courseStatus;
    }
}
