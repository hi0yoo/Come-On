package com.comeon.meetingservice.web.meetingplace.request;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingPlaceAddRequest {

    @NotNull
    private String name;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    public MeetingPlaceAddDto toDto(Long meetingId) {
        return MeetingPlaceAddDto.builder()
                .meetingId(meetingId)
                .name(name)
                .lat(lat)
                .lng(lng)
                .build();
    }

}
