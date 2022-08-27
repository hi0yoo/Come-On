package com.comeon.courseservice.domain.course.service.dto;

import com.comeon.courseservice.domain.course.entity.Course;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CourseDto {

    private Long userId;
    private String title;
    private String description;
    private CourseImageDto courseImageDto;

    private List<CoursePlaceDto> coursePlaceDtos;

    @Builder
    public CourseDto(Long userId, String title, String description, CourseImageDto courseImageDto, List<CoursePlaceDto> coursePlaceDtos) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.courseImageDto = courseImageDto;
        this.coursePlaceDtos = coursePlaceDtos;
    }

    public Course toEntity() {
        Course course = Course.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .courseImage(courseImageDto.toEntity())
                .build();

        coursePlaceDtos.forEach(
                coursePlaceDto -> coursePlaceDto.toEntity(course)
        );

        return course;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setCourseImageDto(CourseImageDto courseImageDto) {
        this.courseImageDto = courseImageDto;
    }
}
