package com.comeon.meetingservice.web.meetinguser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingUserAddResponse {

    private Long meetingId;
    private Long meetingUserId;

}
