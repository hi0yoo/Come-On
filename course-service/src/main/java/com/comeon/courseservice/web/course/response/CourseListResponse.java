package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CourseListResponse {

    private Long courseId;
    private String title;
    private UserDetailInfo writer;
    private String imageUrl;
    private LocalDate lastModifiedDate;

    private Integer likeCount;
    private Boolean userLiked;

    private FirstPlace firstPlace;

    @Builder
    public CourseListResponse(Course course, CoursePlace coursePlace, Double firstPlaceDistance,
                              UserDetailInfo writer, String imageUrl, Boolean userLiked) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.imageUrl = imageUrl;
        this.likeCount = course.getLikeCount();
        this.lastModifiedDate = course.getLastModifiedDate().toLocalDate();
        this.firstPlace = FirstPlace.builder()
                .coursePlace(coursePlace)
                .distance(firstPlaceDistance)
                .build();
        this.writer = writer;
        this.userLiked = userLiked;
    }

    @Getter
    public static class FirstPlace {

        private Long coursePlaceId;
        private Double lat;
        private Double lng;
        private Double distance;

        @Builder
        public FirstPlace(CoursePlace coursePlace, Double distance) {
            this.coursePlaceId = coursePlace.getId();
            this.lat = coursePlace.getLat();
            this.lng = coursePlace.getLng();
            this.distance = distance;
        }
    }
}
