package com.comeon.courseservice.web.courseplace.response;

import lombok.Getter;

@Getter
public class CoursePlacesBatchUpdateResponse {

    public static final String SUCCESS_MESSAGE = "코스 장소 리스트 업데이트가 성공적으로 완료되었습니다.";

    private String message;

    public CoursePlacesBatchUpdateResponse() {
        this.message = SUCCESS_MESSAGE;
    }

    public CoursePlacesBatchUpdateResponse(String message) {
        this.message = message;
    }
}
