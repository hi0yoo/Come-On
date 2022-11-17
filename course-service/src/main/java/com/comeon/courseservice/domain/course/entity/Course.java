package com.comeon.courseservice.domain.course.entity;

import com.comeon.courseservice.domain.common.BaseTimeEntity;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "course_image_id", nullable = false)
    private CourseImage courseImage;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    private CourseStatus courseStatus;

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
    private LocalDateTime updatedDate;

    @Builder
    public Course(Long userId, String title, String description, CourseImage courseImage) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.courseImage = courseImage;
        this.courseStatus = CourseStatus.WRITING;
        this.updatedDate = LocalDateTime.now();

        this.likeCount = 0;
    }

    public void addCoursePlace(CoursePlace coursePlace) {
        coursePlaces.add(coursePlace);
    }

    public void updateCourseState() {
        updatedDate = LocalDateTime.now();

        if (this.coursePlaces.size() == 0) {
            this.courseStatus = CourseStatus.DISABLED;
        } else {
            this.courseStatus = CourseStatus.COMPLETE;
        }
    }

    public void availableCourse() {
        if (this.courseStatus != CourseStatus.COMPLETE) {
            this.courseStatus = CourseStatus.COMPLETE;
        }
    }

    public void disabledCourse() {
        this.courseStatus = CourseStatus.DISABLED;
    }

    public boolean isWritingComplete() {
        return this.courseStatus == CourseStatus.COMPLETE;
    }

    public void increaseLikeCount() {
        likeCount++;
    }

    public void decreaseLikeCount() {
        likeCount--;
    }

    public void updateCourseInfo(String title, String description) {
        updateTitle(title);
        updateDescription(description);
        updatedDate = LocalDateTime.now();
    }

    private void updateTitle(String title) {
        this.title = title;
    }

    private void updateDescription(String description) {
        this.description = description;
    }
}
