package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceSaveRequest {

    @NotNull
    private Long courseId;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.builder()
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .build();
    }
}
