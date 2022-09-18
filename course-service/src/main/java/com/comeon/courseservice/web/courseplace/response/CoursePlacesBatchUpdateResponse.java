package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.Getter;

@Getter
public class CoursePlacesBatchUpdateResponse {

    public static final String SUCCESS_MESSAGE = "코스 장소 리스트 업데이트가 성공적으로 완료되었습니다.";

    private Long courseId;
    private CourseStatus courseStatus;

    private String message;

    public CoursePlacesBatchUpdateResponse(Long courseId, CourseStatus courseStatus) {
        this.courseId = courseId;
        this.courseStatus = courseStatus;
        this.message = SUCCESS_MESSAGE;
    }

    public CoursePlacesBatchUpdateResponse(Long courseId, CourseStatus courseStatus, String message) {
        this.courseId = courseId;
        this.courseStatus = courseStatus;
        this.message = message;
    }
}
