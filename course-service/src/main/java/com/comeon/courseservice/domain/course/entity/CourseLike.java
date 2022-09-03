package com.comeon.courseservice.domain.course.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Course course;

    private Long userId;

    @Builder
    public CourseLike(Course course, Long userId) {
        this.course = course;
        this.userId = userId;

        course.increaseLikeCount();
    }
}
