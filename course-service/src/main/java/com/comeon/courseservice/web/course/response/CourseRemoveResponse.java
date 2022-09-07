package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class CourseRemoveResponse {

    public static final String SUCCESS_MESSAGE = "코스 삭제 처리가 완료되었습니다.";

    private String message;

    public CourseRemoveResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CourseRemoveResponse(String message) {
        this.message = message;
    }
}
