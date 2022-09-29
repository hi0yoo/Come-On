package com.comeon.courseservice.domain.courseplace.entity;

import com.comeon.courseservice.domain.common.BaseTimeEntity;
import com.comeon.courseservice.domain.course.entity.Course;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoursePlace extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_place_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column
    private String address;

    @Column(name = "orders", nullable = false)
    private Integer order;

    @Column(nullable = false)
    private Long kakaoPlaceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private CoursePlaceCategory placeCategory;

    @Builder
    public CoursePlace(Course course, String name, String description,
                       Double lat, Double lng, String address, Integer order,
                       Long kakaoPlaceId, CoursePlaceCategory placeCategory) {
        this.course = course;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.order = order;
        this.kakaoPlaceId = kakaoPlaceId;
        this.placeCategory = placeCategory;

        course.addCoursePlace(this);
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateLat(Double lat) {
        this.lat = lat;
    }

    public void updateLng(Double lng) {
        this.lng = lng;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }

    public void updateKakaoPlaceId(Long kakaoPlaceId) {
        this.kakaoPlaceId = kakaoPlaceId;
    }

    public void updatePlaceCategory(CoursePlaceCategory placeCategory) {
        this.placeCategory = placeCategory;
    }
}
