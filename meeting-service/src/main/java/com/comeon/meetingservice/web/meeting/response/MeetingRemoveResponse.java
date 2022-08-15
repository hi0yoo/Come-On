package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingRemoveResponse {

    private Long id;

    public static MeetingRemoveResponse toResponse(MeetingRemoveDto meetingRemoveDto) {
        return MeetingRemoveResponse.builder()
                .id(meetingRemoveDto.getId())
                .build();
    }
}
