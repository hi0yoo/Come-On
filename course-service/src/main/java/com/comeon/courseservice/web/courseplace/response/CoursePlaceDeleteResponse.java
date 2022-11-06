package com.comeon.courseservice.web.courseplace.response;

public class CoursePlaceDeleteResponse {

    private static final String SUCCESS_MESSAGE = "해당 코스 장소가 성공적으로 삭제되었습니다.";

    private String message;

    public CoursePlaceDeleteResponse(String message) {
        this.message = message;
    }

    public CoursePlaceDeleteResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public String getMessage() {
        return message;
    }
}
