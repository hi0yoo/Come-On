package com.comeon.meetingservice.domain.meeting.dto;

import lombok.*;

import java.time.LocalDate;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingAddDto {

    private Long userId;
    private Long courseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String originalFileName;
    private String storedFileName;

}
