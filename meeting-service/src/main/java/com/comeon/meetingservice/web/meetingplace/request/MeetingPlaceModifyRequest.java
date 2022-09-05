package com.comeon.meetingservice.web.meetingplace.request;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingPlaceModifyRequest {

    private String name;
    private Double lat;
    private Double lng;

    private String memo;

    private Integer order;

    public MeetingPlaceModifyDto toDto(Long meetingId, Long id) {
        return MeetingPlaceModifyDto.builder()
                .meetingId(meetingId)
                .id(id)
                .name(name)
                .lat(lat)
                .lng(lng)
                .memo(memo)
                .order(order)
                .build();
    }
}
