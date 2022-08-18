package com.comeon.meetingservice.web.meeting.response.detail;

import com.comeon.meetingservice.domain.meeting.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingUserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
    private String title;

    private List<MeetingDetailUserResponse> meetingUsers;
    private List<MeetingDetailDateResponse> meetingDates;
    private List<MeetingDetailPlaceResponse> meetingPlaces;

    public static MeetingDetailResponse toResponse(MeetingEntity meetingEntity) {
        return MeetingDetailResponse.builder()
                .id(meetingEntity.getId())
                .title(meetingEntity.getTitle())
                .meetingUsers(convertUserResponse(meetingEntity.getMeetingUserEntities()))
                .meetingDates(convertDateResponse(meetingEntity.getMeetingDateEntities()))
                .meetingPlaces(convertPlaceResponse(meetingEntity.getMeetingPlaceEntities()))
                .build();
    }

    private static List<MeetingDetailUserResponse> convertUserResponse(Set<MeetingUserEntity> meetingUserEntities) {
        return meetingUserEntities.stream()
                .map(MeetingDetailUserResponse::toResponse)
                .collect(Collectors.toList());
    }

    private static List<MeetingDetailPlaceResponse> convertPlaceResponse(Set<MeetingPlaceEntity> meetingPlaceEntities) {
        return meetingPlaceEntities.stream()
                .map(MeetingDetailPlaceResponse::toResponse)
                .collect(Collectors.toList());
    }

    private static List<MeetingDetailDateResponse> convertDateResponse(Set<MeetingDateEntity> meetingDateEntities) {
        return meetingDateEntities.stream()
                .map(MeetingDetailDateResponse::toResponse)
                .collect(Collectors.toList());
    }
}
