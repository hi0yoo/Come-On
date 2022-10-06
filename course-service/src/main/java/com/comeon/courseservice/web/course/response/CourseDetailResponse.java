package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CourseDetailResponse {

    private Long courseId;
    private String title;
    private String description;
    private String imageUrl;
    private CourseStatus courseStatus;
    private LocalDate updatedDate;

    private UserDetailInfo writer;

    private Integer likeCount;
    private Boolean userLiked;

    List<CoursePlaceDetailInfo> coursePlaces;

    @Builder
    public CourseDetailResponse(Course course, UserDetailInfo writer, String imageUrl, Boolean userLiked) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.imageUrl = imageUrl;
        this.courseStatus = course.getCourseStatus();
        this.updatedDate = course.getUpdatedDate().toLocalDate();

        this.writer = writer;

        this.likeCount = course.getLikeCount();
        this.userLiked = userLiked;

        this.coursePlaces = course.getCoursePlaces().stream()
                .map(CoursePlaceDetailInfo::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class CoursePlaceDetailInfo {
        private Long id;
        private String name;
        private String description;
        private Double lat;
        private Double lng;
        private String address;
        private Integer order;
        private Long apiId;
        private String category;

        public CoursePlaceDetailInfo(CoursePlace coursePlace) {
            this.id = coursePlace.getId();
            this.name = coursePlace.getName();
            this.description = coursePlace.getDescription();
            this.lat = coursePlace.getLat();
            this.lng = coursePlace.getLng();
            this.address = coursePlace.getAddress();
            this.order = coursePlace.getOrder();
            this.apiId = coursePlace.getKakaoPlaceId();
            this.category = coursePlace.getPlaceCategory().getDescription();
        }
    }
}
