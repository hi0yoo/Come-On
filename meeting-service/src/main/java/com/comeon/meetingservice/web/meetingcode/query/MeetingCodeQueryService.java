package com.comeon.meetingservice.web.meetingcode.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import com.comeon.meetingservice.web.meetingcode.response.MeetingCodeDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingCodeQueryService {

    private final MeetingCodeQueryRepository meetingCodeQueryRepository;

    public MeetingCodeDetailResponse getDetail(Long meetingId, Long id) {
        MeetingCodeEntity meetingCodeEntity = meetingCodeQueryRepository.findById(meetingId, id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임 코드를 찾을 수 없음",
                        ErrorCode.ENTITY_NOT_FOUND));

        return MeetingCodeDetailResponse.toResponse(meetingCodeEntity);
    }

}
