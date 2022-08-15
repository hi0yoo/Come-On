package com.comeon.meetingservice.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingRemoveDto {

    private Long id;
}
