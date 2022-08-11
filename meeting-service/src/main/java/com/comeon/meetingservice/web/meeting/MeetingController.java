package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.ApiResponse;
import com.comeon.meetingservice.web.meeting.request.MeetingSaveRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingSaveResponse;
import com.comeon.meetingservice.web.util.TokenUtils;
import com.comeon.meetingservice.web.util.ValidationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MeetingSaveResponse> meetingAdd(@Validated @ModelAttribute MeetingSaveRequest meetingSaveRequest,
                                                       BindingResult bindingResult,
                                                       HttpServletRequest request) throws JsonProcessingException {
        ValidationUtils.validate(bindingResult);

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        Long userId = TokenUtils.getUserId(token);

        MeetingDto meetingDto = meetingSaveRequest.toDto();
        meetingDto.setHostId(userId);

        Long savedId = meetingService.add(meetingDto);
        return ApiResponse.createDetail(
                MeetingSaveResponse.builder().id(savedId).build());
    }
}
