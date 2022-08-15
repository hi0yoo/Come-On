package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingModifyResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String storedFileName;

    public static MeetingModifyResponse toResponse(MeetingModifyDto meetingModifyDto) {
        return MeetingModifyResponse.builder()
                .id(meetingModifyDto.getId())
                .title(meetingModifyDto.getTitle())
                .startDate(meetingModifyDto.getStartDate())
                .endDate(meetingModifyDto.getEndDate())
                .storedFileName(meetingModifyDto.getStoredFileName())
                .build();
    }
}
