package com.comeon.meetingservice.domain.meetinguser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingUserAddDto {

    private String inviteCode;
    private Long userId;

}
