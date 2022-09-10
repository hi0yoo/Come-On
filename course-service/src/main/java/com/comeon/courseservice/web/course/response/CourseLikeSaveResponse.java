package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class CourseLikeSaveResponse {

    private Long courseLikeId;

    public CourseLikeSaveResponse(Long courseLikeId) {
        this.courseLikeId = courseLikeId;
    }
}
