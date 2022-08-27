package com.comeon.meetingservice.web.meetingdate.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.web.meetingdate.response.MeetingDateDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingDateQueryService {

    private final MeetingDateQueryRepository meetingDateQueryRepository;

    public MeetingDateDetailResponse getDetail(Long id) {
        MeetingDateEntity meetingDateEntity = meetingDateQueryRepository.findByIdFetchDateUser(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));

        return MeetingDateDetailResponse.toResponse(meetingDateEntity);
    }
}
