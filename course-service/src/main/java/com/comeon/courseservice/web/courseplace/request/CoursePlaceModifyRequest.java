package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlaceModifyRequest {

    @NotNull
    private Long coursePlaceId;

    private String name;

    private String description;

    private Double lat;

    private Double lng;

    @NotNull
    private Integer order;

    private Long kakaoPlaceId;

    @ValidEnum(enumClass = CoursePlaceCategory.class, nullable = true)
    private String placeCategory;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.modifyBuilder()
                .coursePlaceId(coursePlaceId)
                .name(name)
                .description(description)
                .lat(lat)
                .lng(lng)
                .order(order)
                .kakaoPlaceId(kakaoPlaceId)
                .placeCategory(convertPlaceCategoryAndGet())
                .build();
    }

    public CoursePlaceCategory convertPlaceCategoryAndGet() {
        if (Objects.nonNull(placeCategory)) {
            return CoursePlaceCategory.of(placeCategory);
        }
        return null;
    }
}
