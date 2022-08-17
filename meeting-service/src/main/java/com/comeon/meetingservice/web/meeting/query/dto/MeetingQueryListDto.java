package com.comeon.meetingservice.web.meeting.query.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class MeetingQueryListDto {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String storedName;

}
