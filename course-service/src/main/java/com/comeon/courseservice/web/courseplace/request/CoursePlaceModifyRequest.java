package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class CoursePlaceModifyRequest {

    private String description;

    private Integer order;

    @ValidEnum(enumClass = CoursePlaceCategory.class, nullable = true)
    private String category;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.modifyBuilder()
                .description(description)
                .order(order)
                .placeCategory(convertPlaceCategoryAndGet())
                .build();
    }

    public CoursePlaceCategory convertPlaceCategoryAndGet() {
        if (Objects.nonNull(category)) {
            return CoursePlaceCategory.of(category);
        }
        return null;
    }
}
