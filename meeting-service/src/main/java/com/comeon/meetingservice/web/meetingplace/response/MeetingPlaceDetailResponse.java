package com.comeon.meetingservice.web.meetingplace.response;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingPlaceDetailResponse {

    private Long id;
    private Long apiId;
    private String category;
    private String name;
    private Double lat;
    private Double lng;
    private String memo;

    public static MeetingPlaceDetailResponse toResponse(MeetingPlaceEntity meetingPlaceEntity) {
        return MeetingPlaceDetailResponse.builder()
                .id(meetingPlaceEntity.getId())
                .apiId(meetingPlaceEntity.getApiId())
                .category(meetingPlaceEntity.getCategory().getKorName())
                .memo(meetingPlaceEntity.getMemo())
                .name(meetingPlaceEntity.getName())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .build();
    }
}
