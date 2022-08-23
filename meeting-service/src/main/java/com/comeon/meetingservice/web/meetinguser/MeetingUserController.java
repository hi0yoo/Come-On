package com.comeon.meetingservice.web.meetinguser;

import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.service.MeetingUserService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.meetinguser.request.MeetingUserAddRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meeting-users")
@RequiredArgsConstructor
public class MeetingUserController {

    private final MeetingUserService meetingUserService;

    @PostMapping
    @ValidationRequired
    public ApiResponse<Long> meetingUserAdd(@Validated @RequestBody MeetingUserAddRequest meetingUserAddRequest,
                                            BindingResult bindingResult,
                                            @UserId Long userId) {
        MeetingUserAddDto meetingUserAddDto = meetingUserAddRequest.toDto();
        meetingUserAddDto.setUserId(userId);
        meetingUserAddDto.setNickname(null); //TODO - User Service와 통신 후 처리
        meetingUserAddDto.setImageLink(null);

        Long savedId = meetingUserService.add(meetingUserAddDto);

        return ApiResponse.createSuccess(savedId);
    }
}
