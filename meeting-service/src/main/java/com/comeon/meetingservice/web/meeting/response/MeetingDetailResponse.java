package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
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
