package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
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
public class MeetingDetailDateResponse {

    private Long id;
    private LocalDate date;
    private Integer userCount;
    private DateStatus dateStatus;
    private Boolean isSelected;

    public static MeetingDetailDateResponse toResponse(MeetingDateEntity meetingDateEntity, Long userId) {

        return MeetingDetailDateResponse.builder()
                .id(meetingDateEntity.getId())
                .date(meetingDateEntity.getDate())
                .userCount(meetingDateEntity.getUserCount())
                .dateStatus(meetingDateEntity.getDateStatus())
                .isSelected(getUserSelectStatus(meetingDateEntity.getDateUserEntities(), userId))
                .build();
    }

    // 요청을 보낸 회원이 해당 날짜를 선택했는지 여부
    private static boolean getUserSelectStatus(List<DateUserEntity> dateUserEntities, Long userId) {
        return dateUserEntities.stream()
                .filter(du -> du.getMeetingUserEntity().getUserId().equals(userId))
                .findAny()
                .isPresent();
    }
}
