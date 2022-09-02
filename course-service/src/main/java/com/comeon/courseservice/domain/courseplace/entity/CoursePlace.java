package com.comeon.courseservice.domain.courseplace.entity;

import com.comeon.courseservice.domain.common.BaseTimeEntity;
import com.comeon.courseservice.domain.course.entity.Course;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePlace extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_place_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private String name;

    private String description;

    private Double lat;

    private Double lng;

    @Column(name = "orders")
    private Integer order;

    @Builder
    public CoursePlace(Course course, String name, String description, Double lat, Double lng, Integer order) {
        this.course = course;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.order = order;

        course.addCoursePlace(this);
    }
}
