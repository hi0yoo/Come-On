package com.comeon.courseservice.domain.course.entity;

import com.comeon.courseservice.domain.common.BaseTimeEntity;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    private Long userId;

    private String title;

    private String description;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "course_image")
    private CourseImage courseImage;

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private CourseWriteStatus writeStatus;

    @Builder
    public Course(Long userId, String title, String description, CourseImage courseImage) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.courseImage = courseImage;
        this.writeStatus = CourseWriteStatus.WRITING;
    }

    public void addCoursePlace(CoursePlace coursePlace) {
        coursePlaces.add(coursePlace);
    }

    public void completeWriting() {
        if (this.writeStatus != CourseWriteStatus.COMPLETE) {
            this.writeStatus = CourseWriteStatus.COMPLETE;
        }
    }
}
