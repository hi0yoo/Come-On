package com.comeon.meetingservice.domain.meetinguser.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserModifyDto;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.domain.meetinguser.repository.MeetingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingUserServiceImpl implements MeetingUserService {

    private final MeetingUserRepository meetingUserRepository;
    private final MeetingRepository meetingRepository;

    @Override
    public Long add(MeetingUserAddDto meetingUserAddDto) {
        MeetingEntity meetingEntity = findMeetingByInviteCode(meetingUserAddDto.getInviteCode());

        // 이미 모임에 가입된 회원인지 확인
        checkParticipation(meetingUserAddDto.getUserId(), meetingEntity.getId());

        // 모임 코드의 유효기간 확인
        verifyExpiration(meetingEntity.getMeetingCodeEntity().getExpiredDate());

        MeetingUserEntity meetingUserEntity = createMeetingUser(meetingUserAddDto);
        meetingUserEntity.addMeetingEntity(meetingEntity);

        meetingUserRepository.save(meetingUserEntity);

        return meetingUserEntity.getId();
    }

    @Override
    public void modify(MeetingUserModifyDto meetingUserModifyDto) {
        // 아직 다중 HOST 지원 하지 않음, HOST로는 수정 불가능
        if (meetingUserModifyDto.getMeetingRole() == MeetingRole.HOST) {
            throw new CustomException("아직 HOST로는 변경할 수 없습니다.",
                    ErrorCode.MODIFY_HOST_NOT_SUPPORT);
        }

        MeetingUserEntity meetingUserEntity = findMeetingUser(meetingUserModifyDto);

        // 조회된 회원의 권한이 HOST인 경우 수정하면 안됨
        if (meetingUserEntity.getMeetingRole() == MeetingRole.HOST) {
            throw new CustomException("권한이 HOST인 회원은 권한 수정이 불가능합니다.",
                    ErrorCode.MODIFY_HOST_IMPOSSIBLE);
        }

        meetingUserEntity.updateMeetingRole(meetingUserModifyDto.getMeetingRole());
    }

    private MeetingUserEntity findMeetingUser(MeetingUserModifyDto meetingUserModifyDto) {
        return meetingUserRepository
                .findById(meetingUserModifyDto.getMeetingId(), meetingUserModifyDto.getId())
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 회원을 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));
    }

    private MeetingEntity findMeetingByInviteCode(String inviteCode) {
        return meetingRepository.findByInviteCodeFetchCode(inviteCode)
                .orElseThrow(() -> new CustomException("해당 초대코드를 가진 모임이 없습니다.",
                        ErrorCode.NONEXISTENT_CODE));
    }

    private void checkParticipation(Long userId, Long meetingId) {
        meetingUserRepository.findByUserAndMeetingId(userId, meetingId)
                .ifPresent(mu -> {
                    throw new CustomException("이미 모임에 가입된 회원입니다.", ErrorCode.USER_ALREADY_PARTICIPATE);
                });
    }

    private void verifyExpiration(LocalDate expiredDate) {
        // 만료 날짜가 현재 날짜보다 이전이라면 (음수가 반환된다면) 유효하지 않음
        if (expiredDate.compareTo(LocalDate.now()) < 0) {
            throw new CustomException("해당 초대코드는 만료되었습니다.", ErrorCode.EXPIRED_CODE);
        }
    }

    private MeetingUserEntity createMeetingUser(MeetingUserAddDto meetingUserAddDto) {
        MeetingUserEntity meetingUserEntity = MeetingUserEntity.builder()
                .userId(meetingUserAddDto.getUserId())
                .meetingRole(MeetingRole.PARTICIPANT)
                .build();
        return meetingUserEntity;
    }
}
