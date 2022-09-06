package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MyPageCourseListResponse {

    private Long courseId;
    private String title;
    private String imageUrl;
    private Integer likeCount;
    private LocalDate lastModifiedDate;

    private UserDetailInfo writer;

    private Long userLikeId;

    @Builder
    public MyPageCourseListResponse(Course course, String writerNickname, String imageUrl, Long courseLikeId) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.likeCount = course.getLikeCount();
        this.lastModifiedDate = course.getLastModifiedDate().toLocalDate();
        this.writer = new UserDetailInfo(course.getUserId(), writerNickname);
        this.userLikeId = courseLikeId;
    }
}
