package com.comeon.meetingservice.web.meetingdate;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateRemoveDto;
import com.comeon.meetingservice.domain.meetingdate.service.MeetingDateService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.meetingdate.query.MeetingDateQueryService;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateAddRequest;
import com.comeon.meetingservice.web.meetingdate.request.MeetingDateModifyRequest;
import com.comeon.meetingservice.web.meetingdate.response.MeetingDateDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/meeting-dates")
@RequiredArgsConstructor
public class MeetingDateController {

    private final MeetingDateService meetingDateService;
    private final MeetingDateQueryService meetingDateQueryService;

    @PostMapping
    @ValidationRequired
    @ResponseStatus(CREATED)
    public ApiResponse<Long> meetingDateAdd(
            @Validated @RequestBody MeetingDateAddRequest meetingDateAddRequest,
            BindingResult bindingResult,
            @UserId Long userId) {

        MeetingDateAddDto meetingDateAddDto = meetingDateAddRequest.toDto();
        meetingDateAddDto.setUserId(userId);

        Long savedId = meetingDateService.add(meetingDateAddDto);

        return ApiResponse.createSuccess(savedId);
    }

    @PatchMapping("/{meetingDateId}")
    @ValidationRequired
    public ApiResponse meetingDateModify(
            @PathVariable("meetingDateId") Long meetingDateId,
            @Validated @RequestBody MeetingDateModifyRequest meetingDateModifyRequest,
            BindingResult bindingResult) {

        MeetingDateModifyDto meetingDateModifyDto = meetingDateModifyRequest.toDto();
        meetingDateModifyDto.setId(meetingDateId);
        meetingDateService.modify(meetingDateModifyDto);

        return ApiResponse.createSuccess();
    }

    @DeleteMapping("/{meetingDateId}")
    public ApiResponse meetingDateRemove(@PathVariable("meetingDateId") Long meetingDateId,
                                         @UserId Long userId) {

        MeetingDateRemoveDto meetingDateRemoveDto = MeetingDateRemoveDto.builder()
                .userId(userId)
                .id(meetingDateId)
                .build();

        meetingDateService.remove(meetingDateRemoveDto);

        return ApiResponse.createSuccess();
    }

    @GetMapping("/{meetingDateId}")
    public ApiResponse<MeetingDateDetailResponse> meetingDateDetail(
            @PathVariable("meetingDateId") Long meetingDateId) {

        MeetingDateDetailResponse meetingDateDetailResponse
                = meetingDateQueryService.getDetail(meetingDateId);

        return ApiResponse.createSuccess(meetingDateDetailResponse);
    }

}
