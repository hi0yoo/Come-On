package com.comeon.meetingservice.domain.meetingdate.dto;

import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingDateModifyDto {

    private Long id;
    private DateStatus dateStatus;

}
