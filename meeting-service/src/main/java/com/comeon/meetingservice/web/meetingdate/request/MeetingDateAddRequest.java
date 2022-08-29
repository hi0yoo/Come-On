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
    private Long meetingId;

    @NotNull
    private LocalDate date;

    public MeetingDateAddDto toDto() {
        return MeetingDateAddDto.builder()
                .meetingId(meetingId)
                .date(date)
                .build();
    }
}
