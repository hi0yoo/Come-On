package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CoursePlaceSaveRequest {
    // TODO 검증 추가

    private Long courseId;

    private String name;
    private String description;
    private Double lat;
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
