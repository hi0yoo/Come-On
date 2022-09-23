package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Getter;

@Getter
public class CoursePlaceDetails {

    private Long coursePlaceId;
    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private Integer order;
    private Long kakaoPlaceId;
    private String placeCategory;

    public CoursePlaceDetails(CoursePlace coursePlace) {
        this.coursePlaceId = coursePlace.getId();
        this.name = coursePlace.getName();
        this.description = coursePlace.getDescription();
        this.lat = coursePlace.getLat();
        this.lng = coursePlace.getLng();
        this.order = coursePlace.getOrder();
        this.kakaoPlaceId = coursePlace.getKakaoPlaceId();
        this.placeCategory = coursePlace.getPlaceCategory().getDescription();
    }
}
