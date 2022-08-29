package com.comeon.meetingservice.web.meetingdate.request;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingDateAddRequest {

    @NotNull
    private LocalDate date;

    public MeetingDateAddDto toDto(Long meetingId, Long userId) {
        return MeetingDateAddDto.builder()
                .meetingId(meetingId)
                .userId(userId)
                .date(date)
                .build();
    }
}
