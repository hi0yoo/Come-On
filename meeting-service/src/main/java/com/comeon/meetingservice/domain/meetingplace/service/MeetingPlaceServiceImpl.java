package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceRemoveDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetingplace.repository.MeetingPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingPlaceServiceImpl implements MeetingPlaceService {

    private final MeetingRepository meetingRepository;
    private final MeetingPlaceRepository meetingPlaceRepository;

    @Override
    public Long add(MeetingPlaceAddDto meetingPlaceAddDto) {
        MeetingEntity meetingEntity = meetingRepository.findByIdFetchPlace(meetingPlaceAddDto.getMeetingId())
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임을 찾을 수 없습니다.", ENTITY_NOT_FOUND));

        Integer order = calculateOrder(meetingEntity.getMeetingPlaceEntities());

        MeetingPlaceEntity meetingPlaceEntity = createMeetingPlace(meetingPlaceAddDto, order);
        meetingPlaceEntity.addMeetingEntity(meetingEntity);

        meetingPlaceRepository.save(meetingPlaceEntity);

        return meetingPlaceEntity.getId();
    }

    @Override
    public void modify(MeetingPlaceModifyDto meetingPlaceModifyDto) {
        MeetingPlaceEntity meetingPlaceEntity
                = findMeetingPlace(meetingPlaceModifyDto.getMeetingId(), meetingPlaceModifyDto.getId());

        if (Objects.nonNull(meetingPlaceModifyDto.getMemo())) {
            meetingPlaceEntity.updateMemo(meetingPlaceModifyDto.getMemo());
        }
        if (Objects.nonNull(meetingPlaceModifyDto.getOrder())) {
            updatePlaceOrder(meetingPlaceEntity, meetingPlaceModifyDto);
        }
        if (isInfoModified(meetingPlaceModifyDto)) {
            updatePlaceInfo(meetingPlaceEntity, meetingPlaceModifyDto);
        }
    }

    @Override
    public void remove(MeetingPlaceRemoveDto meetingPlaceRemoveDto) {
        MeetingPlaceEntity meetingPlaceEntity
                = findMeetingPlace(meetingPlaceRemoveDto.getMeetingId(), meetingPlaceRemoveDto.getId());

        // 삭제하려는 장소 보다 순서가 뒤인 경우 앞당기기
        List<MeetingPlaceEntity> meetingPlaceEntities
                = meetingPlaceRepository.findAllByMeetingId(meetingPlaceEntity.getMeetingEntity().getId());
        decreaseAfterOrder(meetingPlaceEntities, meetingPlaceEntity.getOrder());

        meetingPlaceRepository.delete(meetingPlaceEntity);
    }

    private MeetingPlaceEntity findMeetingPlace(Long meetingId, Long id) {
        return meetingPlaceRepository.findById(meetingId, id).orElseThrow(()
                -> new CustomException("해당 ID와 일치하는 모임 장소를 찾을 수 없습니다.", ENTITY_NOT_FOUND));
    }

    private MeetingPlaceEntity createMeetingPlace(MeetingPlaceAddDto meetingPlaceAddDto, Integer order) {
        return MeetingPlaceEntity.builder()
                .name(meetingPlaceAddDto.getName())
                .lat(meetingPlaceAddDto.getLat())
                .lng(meetingPlaceAddDto.getLng())
                .order(order)
                .build();
    }

    private Integer calculateOrder(Set<MeetingPlaceEntity> meetingPlaceEntities) {
        int lastOrder = meetingPlaceEntities.stream()
                .mapToInt(MeetingPlaceEntity::getOrder)
                .max().orElse(0);
        return lastOrder + 1;
    }

    private void updatePlaceOrder(MeetingPlaceEntity meetingPlaceEntity, MeetingPlaceModifyDto meetingPlaceModifyDto) {
        List<MeetingPlaceEntity> meetingPlaceEntities =
                meetingPlaceRepository.findAllByMeetingId(meetingPlaceEntity.getMeetingEntity().getId());

        Integer existingOrder = meetingPlaceEntity.getOrder(); // 기존 순서(변경 전)
        Integer modifyingOrder = meetingPlaceModifyDto.getOrder(); // 변경할 순서

        // 수정하려는 순서가 기존의 순서보다 작은 경우 (ex. 기존: 4 -> 변경: 2)
        if (existingOrder > modifyingOrder) {

            // 변경 순서보다 크거나 같고, 기존 순서보다 작은 엔티티들로 필터링 (ex. 2 <= x < 4 -> 순서가 2, 3인 엔티티 필터링)
            // 이후 조회된 엔티티들의 순서를 1 증가 (ex. 2, 3 -> 3, 4)
            meetingPlaceEntities.stream()
                    .filter(mp -> mp.getOrder() >= modifyingOrder && mp.getOrder() < existingOrder)
                    .forEach(MeetingPlaceEntity::increaseOrder);

            // 수정하려는 순서가 기존의 순서보다 큰 경우 (ex. 기존: 2 -> 변경: 4)
        } else if (existingOrder < modifyingOrder) {

            // 변경 순서보다 작거나 같고, 기존 순서보다 큰 엔티티들로 필터링 (ex. 4 >= x > 2 -> 순서가 3, 4인 엔티티 필터링)
            // 이후 조회된 엔티티들의 순서를 1 감소 (ex. 3, 4 -> 2, 3)
            meetingPlaceEntities.stream()
                    .filter(mp -> mp.getOrder() <= modifyingOrder && mp.getOrder() > existingOrder)
                    .forEach(MeetingPlaceEntity::decreaseOrder);
        }

        // 최종적으로 변경 순서 값으로 수정 엔티티 순서 변경
        meetingPlaceEntity.updateOrder(modifyingOrder);
    }

    private boolean isInfoModified(MeetingPlaceModifyDto meetingPlaceModifyDto) {
        return Objects.nonNull(meetingPlaceModifyDto.getName())
                && Objects.nonNull(meetingPlaceModifyDto.getLat())
                && Objects.nonNull(meetingPlaceModifyDto.getLng());
    }

    private void updatePlaceInfo(MeetingPlaceEntity meetingPlaceEntity,
                                 MeetingPlaceModifyDto meetingPlaceModifyDto) {
        meetingPlaceEntity.updateInfo(meetingPlaceModifyDto.getName(),
                meetingPlaceModifyDto.getLat(),
                meetingPlaceModifyDto.getLng()
        );
    }

    private void decreaseAfterOrder(List<MeetingPlaceEntity> meetingPlaceEntities, Integer deletedOrder) {
        meetingPlaceEntities.stream()
                .filter(mp -> mp.getOrder() > deletedOrder)
                .forEach(MeetingPlaceEntity::decreaseOrder);
    }
}
