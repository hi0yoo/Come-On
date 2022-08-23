package com.comeon.meetingservice.web.meetingplace.response;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
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

    private String name;
    private Double lat;
    private Double lng;

    public static MeetingPlaceDetailResponse toResponse(MeetingPlaceEntity meetingPlaceEntity) {
        return MeetingPlaceDetailResponse.builder()
                .name(meetingPlaceEntity.getName())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .build();
    }
}
