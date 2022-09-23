package com.comeon.courseservice.domain.course.service.dto;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CourseDto {

    private Long userId;
    private String title;
    private String description;
    private CourseImageDto courseImageDto;

    @Builder
    public CourseDto(Long userId, String title, String description, CourseImageDto courseImageDto) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.courseImageDto = courseImageDto;
    }

    public Course toEntity() {
        return Course.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .courseImage(courseImageDto.toEntity())
                .build();
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCourseImageDto(CourseImageDto courseImageDto) {
        this.courseImageDto = courseImageDto;
    }
}
