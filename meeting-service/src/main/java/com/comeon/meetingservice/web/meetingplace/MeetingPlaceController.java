package com.comeon.meetingservice.web.meetingplace;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceSaveDto;
import com.comeon.meetingservice.domain.meetingplace.service.MeetingPlaceService;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.util.ValidationUtils;
import com.comeon.meetingservice.web.meetingplace.request.MeetingPlaceSaveRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("/meeting-places")
@RequiredArgsConstructor
public class MeetingPlaceController {

    private final MeetingPlaceService meetingPlaceService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ApiResponse<Long> meetingPlaceAdd(@RequestBody @Validated MeetingPlaceSaveRequest meetingPlaceSaveRequest,
                                             BindingResult bindingResult) {
        ValidationUtils.validate(bindingResult);

        MeetingPlaceSaveDto meetingPlaceSaveDto = meetingPlaceSaveRequest.toDto();

        Long savedId = meetingPlaceService.add(meetingPlaceSaveDto);

        return ApiResponse.createSuccess(savedId);
    }

}
