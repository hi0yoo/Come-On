package com.comeon.meetingservice.web.meetinguser.request;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserModifyDto;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingUserModifyRequest {

    @NotNull
    private MeetingRole meetingRole;

    public MeetingUserModifyDto toDto(Long id, Long meetingId) {
        return MeetingUserModifyDto.builder()
                .meetingRole(meetingRole)
                .meetingId(meetingId)
                .id(id)
                .build();
    }

}
