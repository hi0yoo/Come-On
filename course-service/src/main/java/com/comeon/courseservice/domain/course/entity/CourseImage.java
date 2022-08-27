package com.comeon.courseservice.domain.course.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_img_id")
    private Long id;

    private String originalName;

    private String storedName;

    @Builder
    public CourseImage(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

}
