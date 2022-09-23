package com.comeon.meetingservice.web.meetingdate;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateRemoveDto;
import com.comeon.meetingservice.domain.meetingdate.service.MeetingDateService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.interceptor.MeetingAuth;
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
@RequestMapping("/meetings/{meetingId}/dates")
@RequiredArgsConstructor
public class MeetingDateController {

    private final MeetingDateService meetingDateService;
    private final MeetingDateQueryService meetingDateQueryService;

    @PostMapping
    @ValidationRequired
    @ResponseStatus(CREATED)
    @MeetingAuth(meetingRoles = {MeetingRole.HOST, MeetingRole.EDITOR, MeetingRole.PARTICIPANT})
    public ApiResponse<Long> meetingDateAdd(
            @PathVariable("meetingId") Long meetingId,
            @Validated @RequestBody MeetingDateAddRequest meetingDateAddRequest,
            BindingResult bindingResult,
            @UserId Long userId) {

        MeetingDateAddDto meetingDateAddDto = meetingDateAddRequest.toDto(meetingId, userId);

        Long savedId = meetingDateService.add(meetingDateAddDto);

        return ApiResponse.createSuccess(savedId);
    }

    @PatchMapping("/{dateId}")
    @ValidationRequired
    @MeetingAuth(meetingRoles = {MeetingRole.HOST})
    public ApiResponse meetingDateModify(
            @PathVariable("meetingId") Long meetingId,
            @PathVariable("dateId") Long id,
            @Validated @RequestBody MeetingDateModifyRequest meetingDateModifyRequest,
            BindingResult bindingResult) {

        MeetingDateModifyDto meetingDateModifyDto = meetingDateModifyRequest.toDto(meetingId, id);
        meetingDateService.modify(meetingDateModifyDto);

        return ApiResponse.createSuccess();
    }

    @DeleteMapping("/{dateId}")
    @MeetingAuth(meetingRoles = {MeetingRole.HOST, MeetingRole.EDITOR, MeetingRole.PARTICIPANT})
    public ApiResponse meetingDateRemove(@PathVariable("meetingId") Long meetingId,
                                         @PathVariable("dateId") Long id,
                                         @UserId Long userId) {

        MeetingDateRemoveDto meetingDateRemoveDto = MeetingDateRemoveDto.builder()
                .meetingId(meetingId)
                .userId(userId)
                .id(id)
                .build();

        meetingDateService.remove(meetingDateRemoveDto);

        return ApiResponse.createSuccess();
    }

    @GetMapping("/{dateId}")
    @MeetingAuth(meetingRoles = {MeetingRole.HOST, MeetingRole.EDITOR, MeetingRole.PARTICIPANT})
    public ApiResponse<MeetingDateDetailResponse> meetingDateDetail(
            @PathVariable("meetingId") Long meetingId,
            @PathVariable("dateId") Long id) {

        MeetingDateDetailResponse meetingDateDetailResponse
                = meetingDateQueryService.getDetail(meetingId, id);

        return ApiResponse.createSuccess(meetingDateDetailResponse);
    }

}
