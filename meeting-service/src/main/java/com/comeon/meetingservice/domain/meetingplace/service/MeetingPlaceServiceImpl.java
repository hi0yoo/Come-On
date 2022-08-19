package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceSaveDto;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetingplace.repository.MeetingPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingPlaceServiceImpl implements MeetingPlaceService {

    private final MeetingRepository meetingRepository;
    private final MeetingPlaceRepository meetingPlaceRepository;

    @Override
    public Long add(MeetingPlaceSaveDto meetingPlaceSaveDto) {
        MeetingEntity meetingEntity = meetingRepository.findByIdFetchPlace(meetingPlaceSaveDto.getMeetingId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID와 일치하는 모임을 찾을 수 없습니다."));

        Integer order = calculateOrder(meetingEntity.getMeetingPlaceEntities());

        MeetingPlaceEntity meetingPlaceEntity = createMeetingPlace(meetingPlaceSaveDto, order);
        meetingPlaceEntity.addMeetingEntity(meetingEntity);

        meetingPlaceRepository.save(meetingPlaceEntity);

        return meetingPlaceEntity.getId();
    }

    private MeetingPlaceEntity createMeetingPlace(MeetingPlaceSaveDto meetingPlaceSaveDto, Integer order) {
        return MeetingPlaceEntity.builder()
                .name(meetingPlaceSaveDto.getName())
                .lat(meetingPlaceSaveDto.getLat())
                .lng(meetingPlaceSaveDto.getLng())
                .order(order)
                .build();
    }

    private Integer calculateOrder(Set<MeetingPlaceEntity> meetingPlaceEntities) {
        int lastOrder = meetingPlaceEntities.stream()
                .mapToInt(MeetingPlaceEntity::getOrder)
                .max().orElse(0);
        return lastOrder + 1;
    }
}
