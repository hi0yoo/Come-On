package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.web.meeting.query.dto.MeetingQueryListDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingListResponse {

    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageLink;

    public static MeetingListResponse toResponse(MeetingQueryListDto meetingQueryListDto, String imageLink) {
        return MeetingListResponse.builder()
                .id(meetingQueryListDto.getId())
                .title(meetingQueryListDto.getTitle())
                .startDate(meetingQueryListDto.getStartDate())
                .endDate(meetingQueryListDto.getEndDate())
                .imageLink(imageLink)
                .build();
    }

}
