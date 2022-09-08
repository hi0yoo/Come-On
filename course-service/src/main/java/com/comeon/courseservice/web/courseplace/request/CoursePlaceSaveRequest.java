package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceSaveRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    // 추가
    private Long kakaoPlaceId;

    @ValidEnum(enumClass = CoursePlaceCategory.class)
    private CoursePlaceCategory placeCategory;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.builder()
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .kakaoPlaceId(kakaoPlaceId)
                .placeCategory(placeCategory)
                .build();
    }
}
