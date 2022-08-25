package com.comeon.meetingservice.web.meetingdate.request;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingDateModifyRequest {

    @NotNull
    private DateStatus dateStatus;

    public MeetingDateModifyDto toDto() {
        return MeetingDateModifyDto.builder()
                .dateStatus(dateStatus)
                .build();
    }
}
