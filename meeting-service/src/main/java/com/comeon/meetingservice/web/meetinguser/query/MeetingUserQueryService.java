package com.comeon.meetingservice.web.meetinguser.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.meetinguser.response.MeetingUserAddResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingUserQueryService {

    private final MeetingUserQueryRepository meetingUserQueryRepository;

    public MeetingUserAddResponse getMeetingIdAndUserId(Long id) {
        MeetingUserEntity meetingUserEntity = meetingUserQueryRepository.findById(id).orElseThrow(() ->
                new CustomException("해당 ID와 일치하는 모임 유저를 찾을 수 없음", ErrorCode.ENTITY_NOT_FOUND));

        return MeetingUserAddResponse.builder()
                .meetingId(meetingUserEntity.getMeetingEntity().getId())
                .meetingUserId(meetingUserEntity.getId())
                .build();
    }
}
