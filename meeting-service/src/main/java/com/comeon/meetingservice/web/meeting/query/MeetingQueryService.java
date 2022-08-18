package com.comeon.meetingservice.web.meeting.query;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.web.meeting.response.*;
import com.comeon.meetingservice.web.meeting.response.detail.MeetingDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingQueryService {
    private final MeetingQueryRepository meetingQueryRepository;
    public MeetingDetailResponse getDetail(Long id) {
        MeetingEntity meetingEntity = meetingQueryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID와 일치하는 모임이 없습니다."));

        return MeetingDetailResponse.toResponse(meetingEntity);
    }
