package com.comeon.meetingservice.web.common.feign.courseservice.response;

import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class CourseListResponse {

    private Long coursePlaceId;
    private Long apiId;
    private PlaceCategory placeCategory;
    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private Integer order;

}
