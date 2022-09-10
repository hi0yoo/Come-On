package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceModifyRequest {

    @NotNull
    private Long coursePlaceId;

    // TODO null ok / " " x
    private String name;

    private String description;

    private Double lat;

    private Double lng;

    @NotNull
    private Integer order;

    private Long kakaoPlaceId;

    // TODO Enum null OK 처리 해야함.
    @ValidEnum(enumClass = CoursePlaceCategory.class)
    private CoursePlaceCategory placeCategory;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.modifyBuilder()
                .coursePlaceId(coursePlaceId)
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .order(order)
                .kakaoPlaceId(kakaoPlaceId)
                .placeCategory(placeCategory)
                .build();
    }
}
