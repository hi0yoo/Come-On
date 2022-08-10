package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.ApiResponse;
import com.comeon.meetingservice.web.meeting.request.MeetingSaveRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingSaveResponse;
import com.comeon.meetingservice.web.util.ValidationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final ValidationUtils validationUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MeetingSaveResponse> meetingAdd(@Validated @ModelAttribute MeetingSaveRequest meetingSaveRequest,
                                                       BindingResult bindingResult) throws JsonProcessingException {
        validationUtils.validate(bindingResult);

        MeetingDto meetingDto = meetingSaveRequest.toDto();
        meetingDto.setHostId(1L);
        Long savedId = meetingService.add(meetingDto);

        return ApiResponse.createDetail(
                MeetingSaveResponse.builder().id(savedId).build());
    }
}
