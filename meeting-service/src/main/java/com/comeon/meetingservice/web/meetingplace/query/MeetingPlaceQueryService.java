package com.comeon.meetingservice.web.meetingplace.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.web.common.response.ListResponse;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceDetailResponse;
import com.comeon.meetingservice.web.meetingplace.response.MeetingPlaceListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingPlaceQueryService {

    private final MeetingPlaceQueryRepository meetingPlaceQueryRepository;

    public ListResponse<MeetingPlaceListResponse> getList(Long meetingId) {
        List<MeetingPlaceEntity> meetingPlaceEntities = meetingPlaceQueryRepository.findAllByMeetingId(meetingId);

        List<MeetingPlaceListResponse> meetingPlaceListResponses = meetingPlaceEntities.stream()
                .map(MeetingPlaceListResponse::toResponse)
                .collect(Collectors.toList());

        return ListResponse.createListResponse(meetingPlaceListResponses);
    }

    public MeetingPlaceDetailResponse getDetail(Long meetingId, Long id) {
        MeetingPlaceEntity meetingPlaceEntity = meetingPlaceQueryRepository.findById(meetingId, id).orElseThrow(() ->
                new CustomException("해당 ID와 일치하는 모임 장소를 찾을 수 없습니다.", ENTITY_NOT_FOUND));

        return MeetingPlaceDetailResponse.toResponse(meetingPlaceEntity);
    }
}
