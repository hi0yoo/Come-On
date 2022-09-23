package com.comeon.meetingservice.web.meetingplace.request;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingPlaceAddRequest {

    @NotNull
    private Long apiId;

    @NotNull
    private String name;

    @NotNull
    private Double lat;

    @NotNull
    private Double lng;

    @NotNull
    private PlaceCategory category;

    private String memo;

    public MeetingPlaceAddDto toDto(Long meetingId) {
        return MeetingPlaceAddDto.builder()
                .meetingId(meetingId)
                .apiId(apiId)
                .name(name)
                .lat(lat)
                .lng(lng)
                .category(category)
                .memo(memo)
                .build();
    }

}
