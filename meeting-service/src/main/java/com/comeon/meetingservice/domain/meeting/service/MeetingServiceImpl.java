package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
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
    public Long add(MeetingDto meetingDto) {
        // 모임 이미지 정보 저장
        MeetingFileEntity meetingFileEntity = createMeetingFile(meetingDto);

        // 모임 초대 코드 생성 및 저장
        MeetingCodeEntity meetingCodeEntity = createMeetingCode();

        // 모임 회원 저장
        MeetingUserEntity meetingUserEntity = createMeetingUser(meetingDto);

        // 모임 저장
        MeetingEntity meetingEntity = createMeeting(meetingDto);
        meetingEntity.addMeetingFileEntity(meetingFileEntity);
        meetingEntity.addMeetingCodeEntity(meetingCodeEntity);
        meetingEntity.addMeetingUserEntity(meetingUserEntity);

        meetingRepository.save(meetingEntity);

        // 모임 장소 저장 - 코스로부터 생성한 경우
        if (Objects.nonNull(meetingDto.getCourseId())) {
            //TODO
            // Course Service와 통신 후 처리할 것
        }

        return meetingEntity.getId();
    }

    @Override
    public MeetingDto modify(MeetingDto meetingDto) {
        MeetingEntity meetingEntity = findMeeting(meetingDto.getMeetingId());

        updateMeeting(meetingDto, meetingEntity);
        updateMeetingFile(meetingDto, meetingEntity);

        // 모임 시작일과 종료일이 변경될 경우 변경된 기간 사이에 포함되지 않는 날짜인 경우 삭제처리
        meetingDateRepository.deleteIfNotBetweenDate(meetingEntity.getId(),
                meetingEntity.getStartDate(),
                meetingEntity.getEndDate());

        return meetingDto;
    }

    private MeetingEntity findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(() ->
                new EntityNotFoundException("해당 ID와 일치하는 모임을 찾을 수 없습니다."));
    }

    private MeetingUserEntity createMeetingUser(MeetingDto meetingDto) {
        return MeetingUserEntity.builder()
                .meetingRole(MeetingRole.HOST)
                .userId(meetingDto.getUserId())
                .build();
    }

    private MeetingEntity createMeeting(MeetingDto meetingDto) {
        return MeetingEntity.builder()
                .title(meetingDto.getTitle())
                .startDate(meetingDto.getStartDate())
                .endDate(meetingDto.getEndDate())
                .build();
    }

    private MeetingCodeEntity createMeetingCode() {
        return MeetingCodeEntity.builder()
                .expiredDay(Integer.valueOf(env.getProperty("meeting-code.expired-day")))
                .inviteCode(createInviteCode())
                .build();
    }

    private MeetingFileEntity createMeetingFile(MeetingDto meetingDto) {
        return MeetingFileEntity.builder()
                .originalName(meetingDto.getOriginalFileName())
                .storedName(meetingDto.getStoredFileName())
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

    private void updateMeetingFile(MeetingDto meetingDto, MeetingEntity meetingEntity) {
        if (Objects.nonNull(meetingDto.getOriginalFileName())) {
            meetingEntity.getMeetingFileEntity().updateOriginalName(meetingDto.getOriginalFileName());
            meetingEntity.getMeetingFileEntity().updateStoredName(meetingDto.getStoredFileName());
        }
    }

    private void updateMeeting(MeetingDto meetingDto, MeetingEntity meetingEntity) {
        meetingEntity.updateTitle(meetingDto.getTitle());
        meetingEntity.updateStartDate(meetingDto.getStartDate());
        meetingEntity.updateEndDate(meetingDto.getEndDate());
    }
}
