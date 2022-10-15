package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingDetailPlaceResponse {

    private Long id;
    private Long apiId;
    private PlaceCategory category;
    private String name;
    private String address;
    private String memo;
    private Double lat;
    private Double lng;
    private Integer order;

    public static MeetingDetailPlaceResponse toResponse(MeetingPlaceEntity meetingPlaceEntity) {
        return MeetingDetailPlaceResponse.builder()
                .id(meetingPlaceEntity.getId())
                .apiId(meetingPlaceEntity.getApiId())
                .category(meetingPlaceEntity.getCategory())
                .name(meetingPlaceEntity.getName())
                .address(meetingPlaceEntity.getAddress())
                .memo(meetingPlaceEntity.getMemo())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .order(meetingPlaceEntity.getOrder())
                .build();
    }
}
