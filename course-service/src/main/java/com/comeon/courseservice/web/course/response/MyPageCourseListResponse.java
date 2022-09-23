package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MyPageCourseListResponse {

    private Long courseId;
    private String title;
    private String imageUrl;
    private CourseStatus courseStatus;
    private LocalDate lastModifiedDate;

    private UserDetailInfo writer;

    private Integer likeCount;
    private Boolean userLiked;

    @Builder
    public MyPageCourseListResponse(Course course, UserDetailInfo writer, String imageUrl, Boolean userLiked) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.courseStatus = course.getCourseStatus();
        this.lastModifiedDate = course.getLastModifiedDate().toLocalDate();

        this.writer = writer;

        this.likeCount = course.getLikeCount();
        this.userLiked = userLiked;
    }
}
