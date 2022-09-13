package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MyPageCourseListResponse {

    private Long courseId;
    private String title;
    private UserDetailInfo writer;
    private String imageUrl;
    private LocalDate lastModifiedDate;

    private Integer likeCount;
    private Boolean userLiked;

    @Builder
    public MyPageCourseListResponse(Course course, UserDetailInfo writer, String imageUrl, Boolean userLiked) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.likeCount = course.getLikeCount();
        this.lastModifiedDate = course.getLastModifiedDate().toLocalDate();
        this.writer = writer;
        this.userLiked = userLiked;
    }
}
