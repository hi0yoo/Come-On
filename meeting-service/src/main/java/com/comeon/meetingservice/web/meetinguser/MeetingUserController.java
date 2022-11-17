package com.comeon.meetingservice.web.meetinguser;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.service.MeetingUserService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.interceptor.MeetingAuth;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryService;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserAddRequest;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserModifyRequest;
import com.comeon.meetingservice.web.meetinguser.response.MeetingUserAddResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
public class MeetingUserController {

    private final MeetingUserService meetingUserService;
    private final MeetingUserQueryService meetingUserQueryService;

    @PostMapping("/meetings/users")
    @ResponseStatus(CREATED)
    @ValidationRequired
    public ApiResponse<MeetingUserAddResponse> meetingUserAdd(
            @Validated @RequestBody MeetingUserAddRequest meetingUserAddRequest,
            BindingResult bindingResult,
            @UserId Long userId) {

        MeetingUserAddDto meetingUserAddDto = meetingUserAddRequest.toDto();
        meetingUserAddDto.setUserId(userId);

        Long savedId = meetingUserService.add(meetingUserAddDto);

        return ApiResponse.createSuccess(meetingUserQueryService.getMeetingIdAndUserId(savedId));
    }

    @PatchMapping("/meetings/{meetingId}/users/{userId}")
    @MeetingAuth(meetingRoles = {MeetingRole.HOST})
    public ApiResponse meetingUserModify(
            @PathVariable("meetingId") Long meetingId,
            @PathVariable("userId") Long id,
            @Validated @RequestBody MeetingUserModifyRequest meetingUserModifyRequest) {

        meetingUserService.modify(meetingUserModifyRequest.toDto(meetingId, id));

        return ApiResponse.createSuccess();
    }
}
