package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
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
public class MeetingDetailResponse {

    private Long id;
    private Long myMeetingUserId;
    private MeetingRole myMeetingRole;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<MeetingDetailUserResponse> meetingUsers;
    private List<MeetingDetailDateResponse> meetingDates;
    private List<MeetingDetailPlaceResponse> meetingPlaces;

    public static MeetingDetailResponse toResponse(MeetingEntity meetingEntity,
                                                   MeetingUserEntity currentUserEntity,
                                                   List<MeetingDetailUserResponse> meetingUsers,
                                                   List<MeetingDetailDateResponse> meetingDates,
                                                   List<MeetingDetailPlaceResponse> meetingPlaces
                                                   ) {
        return MeetingDetailResponse.builder()
                .id(meetingEntity.getId())
                .myMeetingUserId(currentUserEntity.getId())
                .myMeetingRole(currentUserEntity.getMeetingRole())
                .title(meetingEntity.getTitle())
                .startDate(meetingEntity.getPeriod().getStartDate())
                .endDate(meetingEntity.getPeriod().getEndDate())
                .meetingUsers(meetingUsers)
                .meetingDates(meetingDates)
                .meetingPlaces(meetingPlaces)
                .build();
    }

}
