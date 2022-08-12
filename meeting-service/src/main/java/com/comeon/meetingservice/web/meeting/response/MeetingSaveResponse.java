package com.comeon.meetingservice.web.meeting.response;

import lombok.*;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingSaveResponse {

    private Long id;

}
