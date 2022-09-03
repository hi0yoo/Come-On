package com.comeon.meetingservice.domain.meetingcode.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingcode.dto.MeetingCodeModifyDto;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import com.comeon.meetingservice.domain.meetingcode.repository.MeetingCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingCodeServiceImpl implements MeetingCodeService {

    private final MeetingCodeRepository meetingCodeRepository;
    private final Environment env;

    @Override
    public void modify(MeetingCodeModifyDto meetingCodeModifyDto) {
        MeetingCodeEntity meetingCodeEntity = findMeetingCode(meetingCodeModifyDto.getId());

        checkExpiration(meetingCodeEntity.getExpiredDate());

        meetingCodeEntity.renewCode(createInviteCode(),
                Integer.valueOf(env.getProperty("meeting-code.expired-day")));
    }

    private MeetingCodeEntity findMeetingCode(Long id) {
        return meetingCodeRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임 코드를 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));
    }

    private void checkExpiration(LocalDate expiredDate) {
        // 만료 날짜가 현재 날짜보다 이후거나 같다면 아직 사용 가능한 토큰임
        if (expiredDate.compareTo(LocalDate.now()) >= 0) {
            throw new CustomException("해당 초대코드가 아직 만료되지 않았습니다.", ErrorCode.UNEXPIRED_CODE);
        }
    }

    private String createInviteCode() {
        String inviteCode;

        do {
            inviteCode = UUID.randomUUID().toString()
                    .replaceAll("-", "").substring(0, 6).toUpperCase();
        } while (meetingCodeRepository.findByInviteCode(inviteCode).isPresent());

        return inviteCode;
    }
}
