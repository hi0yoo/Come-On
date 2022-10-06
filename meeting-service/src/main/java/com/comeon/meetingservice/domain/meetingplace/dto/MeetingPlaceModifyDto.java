package com.comeon.meetingservice.domain.meetingplace.dto;

import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingPlaceModifyDto {

    private Long meetingId;
    private Long id;

    private Long apiId;
    private PlaceCategory category;
    private String name;
    private String address;
    private Double lat;
    private Double lng;

    private String memo;

    private Integer order;
}
