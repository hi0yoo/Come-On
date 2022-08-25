package com.comeon.meetingservice.web.meetingdate;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.service.MeetingDateService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateAddRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/meeting-dates")
@RequiredArgsConstructor
public class MeetingDateController {

    private final MeetingDateService meetingDateService;

    @PostMapping
    @ValidationRequired
    @ResponseStatus(CREATED)
    public ApiResponse<Long> meetingDateAdd(@Validated @RequestBody MeetingDateAddRequest meetingDateAddRequest,
                                            BindingResult bindingResult,
                                            @UserId Long userId) {
        MeetingDateAddDto meetingDateAddDto = meetingDateAddRequest.toDto();
        meetingDateAddDto.setUserId(userId);

        Long savedId = meetingDateService.add(meetingDateAddDto);

        return ApiResponse.createSuccess(savedId);
    }
}
