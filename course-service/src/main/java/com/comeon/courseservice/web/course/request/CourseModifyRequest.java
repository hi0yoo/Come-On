package com.comeon.courseservice.web.course.request;

import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseModifyRequest {

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
