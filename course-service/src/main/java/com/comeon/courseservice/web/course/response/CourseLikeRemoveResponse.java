package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class CourseLikeRemoveResponse {

    public static final String SUCCESS_MESSAGE = "좋아요 삭제 처리가 완료되었습니다.";

    private String message;

    public CourseLikeRemoveResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CourseLikeRemoveResponse(String message) {
        this.message = message;
    }
}
