package com.comeon.meetingservice.web.meetingplace.response;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)

public class MeetingPlaceDetailResponse {

    private Long id;
    private Long apiId;
    private String category;
    private String name;
    private String address;
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
                .address(meetingPlaceEntity.getAddress())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .build();
    }
}
