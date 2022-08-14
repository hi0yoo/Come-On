package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingModResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String originalFileName;
    private String storedFileName;

    public static MeetingModResponse toResponse(MeetingDto meetingDto) {
        return MeetingModResponse.builder()
                .id(meetingDto.getMeetingId())
                .title(meetingDto.getTitle())
                .startDate(meetingDto.getStartDate())
                .endDate(meetingDto.getEndDate())
                .storedFileName(meetingDto.getStoredFileName())
                .originalFileName(meetingDto.getOriginalFileName())
                .build();
    }
}
