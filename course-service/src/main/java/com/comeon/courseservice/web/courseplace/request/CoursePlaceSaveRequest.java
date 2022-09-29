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

    private String address;

    @NotNull
    private Integer order;

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
                .order(order)
                .kakaoPlaceId(apiId)
                .placeCategory(convertPlaceCategoryAndGet())
                .build();
    }

    public CoursePlaceCategory convertPlaceCategoryAndGet() {
        return CoursePlaceCategory.of(category);
    }
}
