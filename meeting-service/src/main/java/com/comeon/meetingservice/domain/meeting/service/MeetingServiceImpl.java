package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import com.comeon.meetingservice.domain.meeting.entity.*;
import com.comeon.meetingservice.domain.meeting.repository.MeetingCodeRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingDateRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final Environment env;
    private final MeetingRepository meetingRepository;
    private final MeetingCodeRepository meetingCodeRepository;
    private final MeetingDateRepository meetingDateRepository;

    @Override
    public MeetingSaveDto add(MeetingSaveDto meetingSaveDto) {
        // 모임 이미지 정보 저장
        MeetingFileEntity meetingFileEntity = createMeetingFile(meetingSaveDto);

        // 모임 초대 코드 생성 및 저장
        MeetingCodeEntity meetingCodeEntity = createMeetingCode();

        // 모임 회원 저장
        MeetingUserEntity meetingUserEntity = createMeetingUser(meetingSaveDto);

        // 모임 저장
        MeetingEntity meetingEntity = createMeeting(meetingSaveDto);
        meetingEntity.addMeetingFileEntity(meetingFileEntity);
        meetingEntity.addMeetingCodeEntity(meetingCodeEntity);
        meetingEntity.addMeetingUserEntity(meetingUserEntity);
        // 모임 장소 저장 - 코스로부터 생성한 경우
        if (Objects.nonNull(meetingSaveDto.getCourseId())) {
            //TODO
            // Course Service와 통신 후 처리할 것
        }

        meetingRepository.save(meetingEntity);
        meetingSaveDto.setId(meetingEntity.getId());

        return meetingSaveDto;
    }

    @Override
    public MeetingModifyDto modify(MeetingModifyDto meetingModifyDto) {
        MeetingEntity meetingEntity = findMeeting(meetingModifyDto.getId());

        updateMeeting(meetingModifyDto, meetingEntity);
        updateMeetingFile(meetingModifyDto, meetingEntity);

        // 모임 시작일과 종료일이 변경될 경우 변경된 기간 사이에 포함되지 않는 날짜인 경우 삭제처리
        meetingDateRepository.deleteIfNotBetweenDate(meetingEntity.getId(),
                meetingEntity.getStartDate(),
                meetingEntity.getEndDate());

        return meetingModifyDto;
    }

    @Override
    public MeetingRemoveDto remove(MeetingRemoveDto meetingRemoveDto) {
        meetingRepository.deleteById(meetingRemoveDto.getId());
        return meetingRemoveDto;
    }

    private MeetingEntity findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(() ->
                new EntityNotFoundException("해당 ID와 일치하는 모임을 찾을 수 없습니다."));
    }

    private MeetingUserEntity createMeetingUser(MeetingSaveDto meetingSaveDto) {
        return MeetingUserEntity.builder()
                .meetingRole(MeetingRole.HOST)
                .userId(meetingSaveDto.getUserId())
                .build();
    }

    private MeetingEntity createMeeting(MeetingSaveDto meetingSaveDto) {
        return MeetingEntity.builder()
                .title(meetingSaveDto.getTitle())
                .startDate(meetingSaveDto.getStartDate())
                .endDate(meetingSaveDto.getEndDate())
                .build();
    }

    private MeetingCodeEntity createMeetingCode() {
        return MeetingCodeEntity.builder()
                .expiredDay(Integer.valueOf(env.getProperty("meeting-code.expired-day")))
                .inviteCode(createInviteCode())
                .build();
    }

    private MeetingFileEntity createMeetingFile(MeetingSaveDto meetingSaveDto) {
        return MeetingFileEntity.builder()
                .originalName(meetingSaveDto.getOriginalFileName())
                .storedName(meetingSaveDto.getStoredFileName())
                .build();
    }

    private String createInviteCode() {
        String inviteCode;

        do {
            inviteCode = UUID.randomUUID().toString()
                    .replaceAll("-", "").substring(0, 6).toUpperCase();
        } while (meetingCodeRepository.findByInviteCode(inviteCode).isPresent());

        return inviteCode;
    }

    private void updateMeetingFile(MeetingModifyDto meetingModifyDto, MeetingEntity meetingEntity) {
        if (Objects.nonNull(meetingModifyDto.getOriginalFileName())) {
            meetingModifyDto.setBeforeStoredFileName(meetingEntity.getMeetingFileEntity().getStoredName());
            meetingEntity.getMeetingFileEntity().updateOriginalName(meetingModifyDto.getOriginalFileName());
            meetingEntity.getMeetingFileEntity().updateStoredName(meetingModifyDto.getStoredFileName());
        }
    }

    private void updateMeeting(MeetingModifyDto meetingModifyDto, MeetingEntity meetingEntity) {
        meetingEntity.updateTitle(meetingModifyDto.getTitle());
        meetingEntity.updateStartDate(meetingModifyDto.getStartDate());
        meetingEntity.updateEndDate(meetingModifyDto.getEndDate());
    }
}
