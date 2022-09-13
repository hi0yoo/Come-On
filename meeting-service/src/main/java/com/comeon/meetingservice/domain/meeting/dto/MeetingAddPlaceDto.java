package com.comeon.meetingservice.domain.meeting.dto;

import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingAddPlaceDto {

    private Long apiId;
    private PlaceCategory category;
    private String name;
    private String memo;
    private Double lat;
    private Double lng;
    private Integer order;

}
