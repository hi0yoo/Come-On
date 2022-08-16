package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import lombok.*;

import static lombok.AccessLevel.*;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class MeetingSaveResponse {

    private Long id;

    public static MeetingSaveResponse toResponse(MeetingSaveDto meetingSaveDto) {
        return MeetingSaveResponse.builder()
                .id(meetingSaveDto.getId())
                .build();
    }

}
