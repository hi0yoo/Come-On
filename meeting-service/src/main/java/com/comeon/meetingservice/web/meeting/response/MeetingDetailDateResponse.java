package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingDetailDateResponse {

    private Long id;
    private LocalDate date;
    private Integer userCount;

    public static MeetingDetailDateResponse toResponse(MeetingDateEntity meetingDateEntity) {
        return MeetingDetailDateResponse.builder()
                .id(meetingDateEntity.getId())
                .date(meetingDateEntity.getDate())
                .userCount(meetingDateEntity.getUserCount())
                .build();
    }
}
