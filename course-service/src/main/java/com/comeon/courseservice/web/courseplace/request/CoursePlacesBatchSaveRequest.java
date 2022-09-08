package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlacesBatchSaveRequest {

    @Valid
    @NotNull
    private List<CoursePlaceInfo> coursePlaces;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePlaceInfo {

        @NotBlank
        private String name;

        @NotBlank
        private String description;

        @NotNull
        private Double lat;

        @NotNull
        private Double lng;

        @NotNull
        private Integer order;

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
                    .order(order)
                    .kakaoPlaceId(kakaoPlaceId)
                    .placeCategory(placeCategory)
                    .build();
        }
    }
}
