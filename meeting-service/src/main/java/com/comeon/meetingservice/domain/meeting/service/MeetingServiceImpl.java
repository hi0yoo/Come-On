package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.entity.*;
import com.comeon.meetingservice.domain.meeting.exception.*;
import com.comeon.meetingservice.domain.meeting.repository.MeetingCodeRepository;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.util.fileupload.FileUploader;
import com.comeon.meetingservice.domain.util.fileupload.UploadFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final Environment env;
    private final MeetingRepository meetingRepository;
    private final MeetingCodeRepository meetingCodeRepository;
    private final FileUploader fileUploader;

    @Override
    public Long add(MeetingDto meetingDto) throws IOException {
        // 모임 이미지 저장
        UploadFileDto uploadFileDto = fileUploader.upload(
                meetingDto.getImage(), env.getProperty("meeting-file.dir"));
        MeetingFileEntity meetingFileEntity = createMeetingFile(uploadFileDto);

        // 모임 초대 코드 생성 및 저장
        MeetingCodeEntity meetingCodeEntity = createMeetingCode(createInviteCode());

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

    private MeetingUserEntity createMeetingUser(MeetingDto meetingDto) {
        return MeetingUserEntity.builder()
                .meetingRole(MeetingRole.HOST)
                .userId(meetingDto.getHostId())
                .build();
    }

    private MeetingEntity createMeeting(MeetingDto meetingDto) {
        return MeetingEntity.builder()
                .title(meetingDto.getTitle())
                .startDate(meetingDto.getStartDate())
                .endDate(meetingDto.getEndDate())
                .build();
    }

    private MeetingCodeEntity createMeetingCode(String inviteCode) {
        return MeetingCodeEntity.builder()
                .expiredDay(Integer.valueOf(env.getProperty("meeting-code.expired-day")))
                .inviteCode(inviteCode)
                .build();
    }

    private MeetingFileEntity createMeetingFile(UploadFileDto uploadFileDto) {
        return MeetingFileEntity.builder()
                .originalName(uploadFileDto.getOriginalFileName())
                .storedName(uploadFileDto.getStoredFileName())
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
}
