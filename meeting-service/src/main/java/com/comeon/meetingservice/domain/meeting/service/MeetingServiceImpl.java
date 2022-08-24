package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import com.comeon.meetingservice.domain.meeting.entity.*;
import com.comeon.meetingservice.domain.meeting.repository.MeetingCodeRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingDateRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final Environment env;
    private final MeetingRepository meetingRepository;
    private final MeetingCodeRepository meetingCodeRepository;
    private final MeetingDateRepository meetingDateRepository;
    private final MeetingUserRepository meetingUserRepository;

    @Override
    public Long add(MeetingSaveDto meetingSaveDto) {
        // 모임 이미지 정보 저장
        MeetingFileEntity meetingFileEntity = createMeetingFile(meetingSaveDto);

        // 모임 초대 코드 생성 및 저장
        MeetingCodeEntity meetingCodeEntity = createMeetingCode();

        // 모임 회원 저장 - TODO User Service와 통신한 후 nickname, imageLink값도 추가할 것
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

        return meetingEntity.getId();
    }

    @Override
    public void modify(MeetingModifyDto meetingModifyDto) {
        MeetingEntity meetingEntity = findMeeting(meetingModifyDto.getId());

        updateMeeting(meetingModifyDto, meetingEntity);
        updateMeetingFile(meetingModifyDto, meetingEntity.getMeetingFileEntity());

        // 모임 시작일과 종료일이 변경될 경우 변경된 기간 사이에 포함되지 않는 날짜인 경우 삭제처리
        meetingDateRepository.deleteIfNotBetweenDate(meetingEntity.getId(),
                meetingEntity.getStartDate(),
                meetingEntity.getEndDate());
    }

    @Override
    public void remove(MeetingRemoveDto meetingRemoveDto) {
        // 파라미터로 넘어온 모임 ID를 가지고 모든 모임유저를 조회함
        List<MeetingUserEntity> meetingUserEntities =
                meetingUserRepository.findAllByMeetingId(meetingRemoveDto.getId());

        // 조회된 모임 회원이 없다면, 모임 조차 없기 때문에 경로변수 이상, 예외 발생
        if (meetingUserEntities.isEmpty()) {
            throw new CustomException("해당 ID와 일치하는 모임을 찾을 수 없습니다.", ENTITY_NOT_FOUND);
        }

        // 해당 모임에 속한 유저중, 탈퇴 요청을 보낸 모임유저를 찾음(모임에 속한 회원인지 우선 검증)
        MeetingUserEntity meetingUserEntity =
                findMeetingUser(meetingUserEntities, meetingRemoveDto.getUserId());

        // 만약 조회된 모임 회원이 1명 이라면, 해당 유저가 모임을 최종적으로 탈퇴하는 것이기에 모임 자체를 삭제처리
        if (meetingUserEntities.size() == 1) {
            meetingRepository.delete(meetingUserEntities.get(0).getMeetingEntity());

        // 조회된 모임 회원이 여러명이라면, 탈퇴 요청을 보낸 모임 유저를 찾아서 탈퇴 로직 수행
        } else {
            // 1. 해당 유저를 모임에서 탈퇴처리
            meetingUserRepository.delete(meetingUserEntity);

            // 2. 만약 탈퇴 회원이 HOST라면 다음에 들어온 유저로 HOST를 바꾸기
            if (meetingUserEntity.getMeetingRole().equals(MeetingRole.HOST)) {
                changeHost(meetingUserEntities, meetingRemoveDto.getUserId());
            }
        }
    }

    private MeetingEntity findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(() ->
                new CustomException("해당 ID와 일치하는 모임을 찾을 수 없습니다.", ENTITY_NOT_FOUND));
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

    private void updateMeetingFile(MeetingModifyDto meetingModifyDto, MeetingFileEntity meetingFileEntity) {
        if (Objects.nonNull(meetingModifyDto.getOriginalFileName())) {
            meetingFileEntity.updateOriginalName(meetingModifyDto.getOriginalFileName());
            meetingFileEntity.updateStoredName(meetingModifyDto.getStoredFileName());
        }
    }

    private void updateMeeting(MeetingModifyDto meetingModifyDto, MeetingEntity meetingEntity) {
        meetingEntity.updateTitle(meetingModifyDto.getTitle());
        meetingEntity.updateStartDate(meetingModifyDto.getStartDate());
        meetingEntity.updateEndDate(meetingModifyDto.getEndDate());
    }

    private MeetingUserEntity findMeetingUser(List<MeetingUserEntity> meetingUserEntities, Long userId) {
        return meetingUserEntities.stream()
                .filter(mu -> mu.getUserId().equals(userId))
                .findAny()
                .orElseThrow(()
                        -> new CustomException("모임에 유저가 속해있지 않습니다.", MEETING_USER_NOT_INCLUDE));
    }

    private void changeHost(List<MeetingUserEntity> meetingUserEntities, Long deletedId) {
        MeetingUserEntity nextMeetingUser = meetingUserEntities.stream()
                .filter(m -> !m.getUserId().equals(deletedId))
                .sorted(Comparator.comparing(MeetingUserEntity::getCreatedDateTime))
                .limit(1)
                .findAny()
                .orElseThrow();

        nextMeetingUser.changeMeetingRole(MeetingRole.HOST);
    }
}
