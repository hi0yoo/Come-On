package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceAddRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private String address;

    @NotNull
    private Long apiId;

    @ValidEnum(enumClass = CoursePlaceCategory.class)
    private String category;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.builder()
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .address(address)
                .kakaoPlaceId(apiId)
                .placeCategory(convertPlaceCategoryAndGet())
                .build();
    }

    public CoursePlaceCategory convertPlaceCategoryAndGet() {
        return CoursePlaceCategory.of(category);
    }
}
