package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.request.MeetingSaveRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingSaveResponse;
import com.comeon.meetingservice.web.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

        UploadFileDto uploadFileDto = uploadImage(meetingSaveRequest);

        MeetingDto meetingDto = meetingSaveRequest.toDto();
        meetingDto.setUserId(userId);
        meetingDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
        meetingDto.setStoredFileName(uploadFileDto.getStoredFileName());

        Long savedId;
        try {
            savedId = meetingService.add(meetingDto);
        } catch (RuntimeException e) {
            // TODO 파일 지우는 로직
            throw e;
        }

        return ApiResponse.createSuccess(
                MeetingSaveResponse.builder().id(savedId).build());
    }

    private UploadFileDto uploadImage(MeetingSaveRequest meetingSaveRequest) {
        return fileManager.upload(
                meetingSaveRequest.getImage(),
                env.getProperty("meeting-file.dir"));
    }
}
