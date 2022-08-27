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

    @Builder
    public CoursePlaceDto(String name, String description, Double lat, Double lng) {
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }

    public CoursePlace toEntity(Course course, Integer order) {
        return CoursePlace.builder()
                .course(course)
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .order(order)
                .build();
    }
}
