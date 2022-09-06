package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CourseListResponse {

    private Long courseId;
    private String title;
    private String imageUrl;
    private Integer likeCount;
    private LocalDate lastModifiedDate;
    private Double firstPlaceDistance;

    private UserDetailInfo writer;

    private Long userLikeId;

    @Builder
    public CourseListResponse(Course course, Double firstPlaceDistance, String writerNickname, String imageUrl, Long courseLikeId) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.likeCount = course.getLikeCount();
        this.lastModifiedDate = course.getLastModifiedDate().toLocalDate();
        this.firstPlaceDistance = firstPlaceDistance;
        this.writer = new UserDetailInfo(course.getUserId(), writerNickname);
        this.userLikeId = courseLikeId;
    }

}