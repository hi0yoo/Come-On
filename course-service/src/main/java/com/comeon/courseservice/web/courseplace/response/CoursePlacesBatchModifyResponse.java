package com.comeon.courseservice.web.courseplace.response;

public class CoursePlacesBatchModifyResponse {

    public static final String SUCCESS_MESSAGE = "코스 장소 리스트 수정이 완료되었습니다.";

    private String message;

    public CoursePlacesBatchModifyResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CoursePlacesBatchModifyResponse(String message) {
        this.message = message;
    }
}
