package com.comeon.courseservice.web.course.request;

import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseSaveRequest {

    @NotNull
    private MultipartFile imgFile;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    public CourseDto toServiceDto() {
        return CourseDto.builder()
                .title(title)
                .description(description)
                .build();
    }
}
