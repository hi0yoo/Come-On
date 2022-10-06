package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Getter;

@Getter
public class CoursePlaceDetails {

    private Long id;
    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private String address;
    private Integer order;
    private Long apiId;
    private String category;

    public CoursePlaceDetails(CoursePlace coursePlace) {
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
