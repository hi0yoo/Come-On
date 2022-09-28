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
    private Long id;

    private String description;

    private Integer order;

    @ValidEnum(enumClass = CoursePlaceCategory.class, nullable = true)
    private String category;

    public CoursePlaceDto toServiceDto() {
        return CoursePlaceDto.modifyBuilder()
                .coursePlaceId(id)
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
