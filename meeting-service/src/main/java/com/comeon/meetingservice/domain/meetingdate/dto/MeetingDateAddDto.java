package com.comeon.meetingservice.domain.meetingdate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingDateAddDto {

    private Long meetingId;
    private Long userId;
    private LocalDate date;

}
