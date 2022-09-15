package com.comeon.courseservice.web.course.response;

import lombok.Getter;

import java.util.Objects;

@Getter
public class CourseLikeUpdateResponse {

    private Boolean userLiked;

    public CourseLikeUpdateResponse(Long courseLikeId) {
        this.userLiked = Objects.nonNull(courseLikeId);
    }
}
