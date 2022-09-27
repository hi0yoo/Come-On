package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CourseListResponse {

    private Long courseId;
    private String title;
    private String imageUrl;
    private CourseStatus courseStatus;
    private LocalDate updatedDate;

    private UserDetailInfo writer;

    private Integer likeCount;
    private Boolean userLiked;

    private FirstPlace firstPlace;

    @Builder
    public CourseListResponse(Course course, CoursePlace coursePlace, Double firstPlaceDistance,
                              UserDetailInfo writer, String imageUrl, Boolean userLiked) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.courseStatus = course.getCourseStatus();
        this.updatedDate = course.getUpdatedDate().toLocalDate();

        this.writer = writer;

        this.likeCount = course.getLikeCount();
        this.userLiked = userLiked;

        this.firstPlace = FirstPlace.builder()
                .coursePlace(coursePlace)
                .distance(firstPlaceDistance)
                .build();
    }

    @Getter
    public static class FirstPlace {

        private Long id;
        private Double lat;
        private Double lng;
        private Double distance;

        @Builder
        public FirstPlace(CoursePlace coursePlace, Double distance) {
            this.id = coursePlace.getId();
            this.lat = coursePlace.getLat();
            this.lng = coursePlace.getLng();
            this.distance = distance;
        }
    }
}
