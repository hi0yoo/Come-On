package com.comeon.meetingservice.web.meetingplace.query;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingPlaceQueryService {

    private final MeetingPlaceQueryRepository meetingPlaceQueryRepository;

    public MeetingPlaceDetailResponse getDetail(Long id) {
        MeetingPlaceEntity meetingPlaceEntity = meetingPlaceQueryRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("해당 ID와 일치하는 모임 장소를 찾을 수 없습니다."));

        return MeetingPlaceDetailResponse.toResponse(meetingPlaceEntity);
    }
}
