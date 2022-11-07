package com.comeon.courseservice.web.courseplace.response;

public class CoursePlaceModifyResponse {

    private static final String SUCCESS_MESSAGE = "해당 코스 장소가 성공적으로 수정되었습니다.";

    private String message;

    public CoursePlaceModifyResponse(String message) {
        this.message = message;
    }

    public CoursePlaceModifyResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public String getMessage() {
        return message;
    }
}
