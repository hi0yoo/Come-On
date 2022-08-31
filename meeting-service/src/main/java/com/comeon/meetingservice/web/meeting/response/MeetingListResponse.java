package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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
    private Long meetingCodeId;
    private String imageLink;
    private List<LocalDate> fixedDates;
    private MeetingStatus meetingStatus;

    public static MeetingListResponse toResponse(MeetingEntity meetingEntity,
                                                 String imageLink,
                                                 List<LocalDate> fixedDates,
                                                 MeetingStatus meetingStatus) {
        return MeetingListResponse.builder()
                .id(meetingEntity.getId())
                .title(meetingEntity.getTitle())
                .startDate(meetingEntity.getPeriod().getStartDate())
                .endDate(meetingEntity.getPeriod().getEndDate())
                .imageLink(imageLink)
                .meetingCodeId(meetingEntity.getMeetingCodeEntity().getId())
                .fixedDates(fixedDates)
                .meetingStatus(meetingStatus)
                .build();
    }

}
