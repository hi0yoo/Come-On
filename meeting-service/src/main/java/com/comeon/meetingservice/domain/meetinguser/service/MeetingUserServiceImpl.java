package com.comeon.meetingservice.domain.meetinguser.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.domain.meetinguser.repository.MeetingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingUserServiceImpl implements MeetingUserService {

    private final MeetingUserRepository meetingUserRepository;
    private final MeetingRepository meetingRepository;

    @Override
    public Long add(MeetingUserAddDto meetingUserAddDto) {
        MeetingEntity meetingEntity = findMeetingByInviteCode(meetingUserAddDto.getInviteCode());

        verifyExpiration(meetingEntity.getMeetingCodeEntity().getExpiredDate());

        MeetingUserEntity meetingUserEntity = createMeetingUser(meetingUserAddDto);
        meetingUserEntity.addMeetingEntity(meetingEntity);

        meetingUserRepository.save(meetingUserEntity);

        return meetingUserEntity.getId();
    }

    private MeetingEntity findMeetingByInviteCode(String inviteCode) {
        return meetingRepository.findByInviteCodeFetchCode(inviteCode)
                .orElseThrow(() -> new CustomException("해당 초대코드를 가진 모임이 없습니다.",
                        ErrorCode.INVALID_MEETING_CODE));
    }

    private void verifyExpiration(LocalDate expiredDate) {
        // 만료 날짜가 현재 날짜보다 이전이라면 (음수가 반환된다면) 유효하지 않음
        if (expiredDate.compareTo(LocalDate.now()) < 0) {
            throw new CustomException("해당 초대코드는 만료되었습니다.", ErrorCode.INVALID_MEETING_CODE);
        }
    }

    private MeetingUserEntity createMeetingUser(MeetingUserAddDto meetingUserAddDto) {
        MeetingUserEntity meetingUserEntity = MeetingUserEntity.builder()
                .userId(meetingUserAddDto.getUserId())
                .meetingRole(MeetingRole.PARTICIPANT)
                .nickName(meetingUserAddDto.getNickname()) //TODO - User Service와 연동 후 작업
                .imageLink(meetingUserAddDto.getImageLink()) //TODO
                .build();
        return meetingUserEntity;
    }
}
