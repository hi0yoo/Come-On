package com.comeon.meetingservice.web.meetingplace.request;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceSaveDto;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingPlaceSaveRequest {

    @NotNull
    private Long meetingId;

    @NotNull
    private String name;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    public MeetingPlaceSaveDto toDto() {
        return MeetingPlaceSaveDto.builder()
                .meetingId(meetingId)
                .name(name)
                .lat(lat)
                .lng(lng)
                .build();
    }

}
