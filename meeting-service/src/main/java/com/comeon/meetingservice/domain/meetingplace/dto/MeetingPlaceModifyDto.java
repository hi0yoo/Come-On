package com.comeon.meetingservice.domain.meetingplace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingPlaceModifyDto {

    private Long id;

    private String name;
    private Double lat;
    private Double lng;

    private String memo;

    private Integer order;
}
