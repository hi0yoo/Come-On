package com.comeon.courseservice.web.course.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CourseDetailResponse {

    private Long courseId;
    private String title;
    private String description;
    private String imageUrl;

    private UserDetailInfo writer;

    List<CoursePlaceDetailInfo> coursePlaces;

    public CourseDetailResponse(Course course, String writerNickname, String imageUrl) {
        this.courseId = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.imageUrl = imageUrl;

        this.writer = new UserDetailInfo(course.getUserId(), writerNickname);

        this.coursePlaces = course.getCoursePlaces().stream()
                .map(CoursePlaceDetailInfo::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class CoursePlaceDetailInfo {
        private Long coursePlaceId;
        private String name;
        private String description;
        private Double lat;
        private Double lng;
        private Integer order;

        public CoursePlaceDetailInfo(CoursePlace coursePlace) {
            this.coursePlaceId = coursePlace.getId();
            this.name = coursePlace.getName();
            this.description = coursePlace.getDescription();
            this.lat = coursePlace.getLat();
            this.lng = coursePlace.getLng();
            this.order = coursePlace.getOrder();
        }
    }

    @Getter
    public static class UserDetailInfo {
        private Long userId;
        private String nickname;

        public UserDetailInfo(Long userId, String nickname) {
            this.userId = userId;
            this.nickname = nickname;
        }
    }
}
