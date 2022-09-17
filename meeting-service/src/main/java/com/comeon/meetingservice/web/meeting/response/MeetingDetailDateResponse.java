package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingDetailDateResponse {

    private Long id;
    private LocalDate date;
    private Integer userCount;
    private DateStatus dateStatus;

    public static MeetingDetailDateResponse toResponse(MeetingDateEntity meetingDateEntity) {
        return MeetingDetailDateResponse.builder()
                .id(meetingDateEntity.getId())
                .date(meetingDateEntity.getDate())
                .userCount(meetingDateEntity.getUserCount())
                .dateStatus(meetingDateEntity.getDateStatus())
                .build();
    }
}
