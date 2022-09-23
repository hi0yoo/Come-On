package com.comeon.courseservice.web.course.query.repository.cond;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CourseCondition {

    // 제목
    private String title;

    // 사용자 좌표
    private Coordinate coordinate;

    public CourseCondition(String title, Double lat, Double lng) {
        this.title = title;
        if (Objects.nonNull(lat) && Objects.nonNull(lng)) {
            this.coordinate = new Coordinate(lat, lng);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Coordinate {

        private Double lat;
        private Double lng;
    }
}
