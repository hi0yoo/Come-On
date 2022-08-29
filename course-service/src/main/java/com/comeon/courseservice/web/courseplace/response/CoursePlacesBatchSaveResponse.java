package com.comeon.courseservice.web.courseplace.response;

import lombok.Getter;

@Getter
public class CoursePlacesBatchSaveResponse {

    public static final String SUCCESS_MESSAGE = "코스 장소 리스트 저장이 완료되었습니다.";

    private String message;

    public CoursePlacesBatchSaveResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CoursePlacesBatchSaveResponse(String message) {
        this.message = message;
    }
}
