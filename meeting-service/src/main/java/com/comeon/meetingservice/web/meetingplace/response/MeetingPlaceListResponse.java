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

public class MeetingPlaceListResponse {

    private Long id;
    private Long apiId;
    private String category;
    private String name;
    private String address;
    private Double lat;
    private Double lng;
    private String memo;
    private Integer order;

    public static MeetingPlaceListResponse toResponse(MeetingPlaceEntity meetingPlaceEntity) {
        return MeetingPlaceListResponse.builder()
                .id(meetingPlaceEntity.getId())
                .apiId(meetingPlaceEntity.getApiId())
                .category(meetingPlaceEntity.getCategory().getKorName())
                .name(meetingPlaceEntity.getName())
                .address(meetingPlaceEntity.getAddress())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .memo(meetingPlaceEntity.getMemo())
                .order(meetingPlaceEntity.getOrder())
                .build();
    }

}
