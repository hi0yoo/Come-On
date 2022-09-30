package com.comeon.meetingservice.web.meetingplace.request;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.entity.PlaceCategory;
import lombok.*;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingPlaceModifyRequest {

    private Long apiId;
    private String name;
    private Double lat;
    private Double lng;
    private PlaceCategory category;
    private String address;

    private String memo;

    private Integer order;

    public MeetingPlaceModifyDto toDto(Long meetingId, Long id) {
        return MeetingPlaceModifyDto.builder()
                .meetingId(meetingId)
                .id(id)
                .apiId(apiId)
                .name(name)
                .address(address)
                .lat(lat)
                .lng(lng)
                .category(category)
                .memo(memo)
                .order(order)
                .build();
    }
}
