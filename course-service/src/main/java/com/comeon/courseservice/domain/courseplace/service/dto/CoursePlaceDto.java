package com.comeon.courseservice.domain.courseplace.service.dto;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CoursePlaceDto {

    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private Integer order;

    // 추가
    private Long mapPlaceId;

    @Builder
    public CoursePlaceDto(String name, String description, Double lat,
                          Double lng, Integer order, Long mapPlaceId) {
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.order = order;
        this.mapPlaceId = mapPlaceId;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public CoursePlace toEntity(Course course) {
        return CoursePlace.builder()
                .course(course)
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .order(order)
                .mapPlaceId(mapPlaceId)
                .build();
    }
}
