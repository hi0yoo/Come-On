package com.comeon.meetingservice.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingModifyDto {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;

    private String originalFileName;
    private String storedFileName;

}
