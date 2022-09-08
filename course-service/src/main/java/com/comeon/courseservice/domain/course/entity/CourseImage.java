package com.comeon.courseservice.domain.course.entity;

import com.comeon.courseservice.domain.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseImage extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_img_id")
    private Long id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Builder
    public CourseImage(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public void updateCourseImage(CourseImage courseImage) {
        this.originalName = courseImage.getOriginalName();
        this.storedName = courseImage.getStoredName();
    }

}
