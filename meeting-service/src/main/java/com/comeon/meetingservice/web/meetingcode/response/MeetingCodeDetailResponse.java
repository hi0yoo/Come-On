package com.comeon.meetingservice.web.meetingcode.response;

import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
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
public class MeetingCodeDetailResponse {

    private Long id;
    private String inviteCode;
    private Boolean isExpired;

    public static MeetingCodeDetailResponse toResponse(MeetingCodeEntity meetingCodeEntity) {
        return MeetingCodeDetailResponse.builder()
                .id(meetingCodeEntity.getId())
                .inviteCode(meetingCodeEntity.getInviteCode())
                .isExpired(meetingCodeEntity.getExpiredDate().compareTo(LocalDate.now()) < 0)
                .build();
    }
}
