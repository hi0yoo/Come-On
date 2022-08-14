package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.request.MeetingModRequest;
import com.comeon.meetingservice.web.meeting.request.MeetingSaveRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingModResponse;
import com.comeon.meetingservice.web.meeting.response.MeetingSaveResponse;
import com.comeon.meetingservice.web.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final Environment env;
    private final MeetingService meetingService;
    private final FileManager fileManager;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MeetingSaveResponse> meetingAdd(@Validated @ModelAttribute MeetingSaveRequest meetingSaveRequest,
                                                       BindingResult bindingResult,
                                                       @UserId Long userId) {
        ValidationUtils.validate(bindingResult);

        UploadFileDto uploadFileDto = uploadImage(meetingSaveRequest.getImage());

        MeetingDto meetingDto = meetingSaveRequest.toDto();
        meetingDto.setUserId(userId);
        meetingDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
        meetingDto.setStoredFileName(uploadFileDto.getStoredFileName());

        Long savedId;
        try {
            savedId = meetingService.add(meetingDto);
        } catch (RuntimeException e) {
            deleteImage(uploadFileDto.getStoredFileName());
            throw e;
        }

        return ApiResponse.createSuccess(
                MeetingSaveResponse.builder().id(savedId).build());
    }

    @PatchMapping("/{meetingId}")
    public ApiResponse<MeetingModResponse> meetingModify(@PathVariable("meetingId") Long meetingId,
                                                         @Validated @ModelAttribute MeetingModRequest meetingModRequest,
                                                         BindingResult bindingResult) {
        ValidationUtils.validate(bindingResult);

        MeetingDto meetingDto = meetingModRequest.toDto();
        meetingDto.setMeetingId(meetingId);

        // 파일은 수정할 수도 안 할 수도 있음
        if (Objects.nonNull(meetingModRequest.getImage())) {
            UploadFileDto uploadFileDto = uploadImage(meetingModRequest.getImage());
            meetingDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
            meetingDto.setStoredFileName(uploadFileDto.getStoredFileName());
        }

        MeetingDto modifiedData = meetingService.modify(meetingDto);

        // TODO 정상적으로 수정 됐다면 기존 파일 삭제

        return ApiResponse.createSuccess(MeetingModResponse.toResponse(modifiedData));
    }

    private UploadFileDto uploadImage(MultipartFile image) {
        return fileManager.upload(
                image,
                env.getProperty("meeting-file.dir"));
    }

    private void deleteImage(String storedFileName) {
        fileManager.delete(
                storedFileName,
                env.getProperty("meeting-file.dir"));
    }
}
