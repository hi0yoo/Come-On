package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class CourseWritingDoneResponse {

    public static final String SUCCESS_MESSAGE = "코스 등록 처리가 완료되었습니다.";

    private String message;

    public CourseWritingDoneResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CourseWritingDoneResponse(String message) {
        this.message = message;
    }
}
