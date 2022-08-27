package com.comeon.courseservice.domain.course.service.dto;

import com.comeon.courseservice.domain.course.entity.CourseImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CourseImageDto {

    private String originalName;
    private String storedName;

    @Builder
    public CourseImageDto(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public CourseImage toEntity() {
        return CourseImage.builder()
                .originalName(originalName)
                .storedName(storedName)
                .build();
    }
}
