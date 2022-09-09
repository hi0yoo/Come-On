package com.comeon.courseservice.web.courseplace.request;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.validation.ValidEnum;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePlacesBatchModifyRequest {

    // TODO coursePlaces null test
    @Valid
    private List<CoursePlaceInfo> coursePlaces;
    /*
    TODO CoursePlaces 검증
        - null이면 모두 다 지우는 것. null 가능한지 테스트하기
        - coursePlaceId 중복 x
        - order 중복 x
     */

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoursePlaceInfo {

        private Long coursePlaceId;

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
            if (Objects.isNull(coursePlaceId)) {
                return CoursePlaceDto.builder()
                        .name(name)
                        .description(description)
                        .lat(lat)
                        .lng(lng)
                        .order(order)
                        .kakaoPlaceId(kakaoPlaceId)
                        .placeCategory(placeCategory)
                        .build();
            } else {
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
    }
}
