//package com.comeon.courseservice.domain.course.entity;
//
//import com.comeon.courseservice.domain.common.BaseTimeEntity;
//import lombok.AccessLevel;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//
//@Entity @Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class CourseLike extends BaseTimeEntity {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "course_like_id")
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "course_id", nullable = false)
//    private Course course;
//
//    @Column(nullable = false)
//    private Long userId;
//
//    @Builder
//    public CourseLike(Course course, Long userId) {
//        this.course = course;
//        this.userId = userId;
//
//        this.course.increaseLikeCount();
//    }
//}
