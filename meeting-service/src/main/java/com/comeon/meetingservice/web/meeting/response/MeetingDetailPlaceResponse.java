package com.comeon.meetingservice.web.meeting.response;

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
public class MeetingDetailPlaceResponse {

    private Long id;
    private String name;
    private String memo;
    private Double lat;
    private Double lng;
    private Integer order;

    public static MeetingDetailPlaceResponse toResponse(MeetingPlaceEntity meetingPlaceEntity) {
        return MeetingDetailPlaceResponse.builder()
                .id(meetingPlaceEntity.getId())
                .name(meetingPlaceEntity.getName())
                .memo(meetingPlaceEntity.getMemo())
                .lat(meetingPlaceEntity.getLat())
                .lng(meetingPlaceEntity.getLng())
                .order(meetingPlaceEntity.getOrder())
                .build();
    }
}
