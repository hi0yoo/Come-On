package com.comeon.meetingservice.domain.meetinguser.dto;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingUserModifyDto {

    private Long id;
    private MeetingRole meetingRole;

}
