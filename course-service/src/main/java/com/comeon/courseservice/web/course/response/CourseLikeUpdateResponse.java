package com.comeon.courseservice.web.course.response;

import lombok.Getter;

import java.util.Objects;

@Getter
public class CourseLikeUpdateResponse {

    private LikeResult likeResult;

    public CourseLikeUpdateResponse(Long courseLikeId) {
        if (Objects.isNull(courseLikeId)) {
            likeResult = LikeResult.DELETED;
        } else {
            likeResult = LikeResult.CREATED;
        }
    }

    @Getter
    public enum LikeResult {
        CREATED("코스에 좋아요가 등록된 경우"),
        DELETED("코스에 등록된 좋아요가 삭제된 경우"),
        ;

        private final String description;

        LikeResult(String description) {
            this.description = description;
        }
    }
}
