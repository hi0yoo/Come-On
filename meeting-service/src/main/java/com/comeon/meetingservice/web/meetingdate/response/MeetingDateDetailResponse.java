package com.comeon.meetingservice.web.meetingdate.response;

import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)

public class MeetingDateDetailResponse {

    private Long id;
    private LocalDate date;
    private Integer userCount;
    private DateStatus dateStatus;

    private List<MeetingDateDetailUserResponse> dateUsers;

    public static MeetingDateDetailResponse toResponse(
            MeetingDateEntity meetingDateEntity,
            List<MeetingDateDetailUserResponse> dateUsers) {

        return MeetingDateDetailResponse.builder()
                .id(meetingDateEntity.getId())
                .date(meetingDateEntity.getDate())
                .userCount(meetingDateEntity.getUserCount())
                .dateStatus(meetingDateEntity.getDateStatus())
                .dateUsers(dateUsers)
                .build();
    }

}
