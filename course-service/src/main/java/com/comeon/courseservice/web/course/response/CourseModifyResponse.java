package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class CourseModifyResponse {

    public static final String SUCCESS_MESSAGE = "코스 수정 처리가 완료되었습니다.";

    private String message;

    public CourseModifyResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CourseModifyResponse(String message) {
        this.message = message;
    }
}
