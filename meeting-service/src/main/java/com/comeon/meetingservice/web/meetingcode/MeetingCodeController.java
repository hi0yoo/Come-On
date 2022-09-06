package com.comeon.meetingservice.web.meetingcode;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingcode.dto.MeetingCodeModifyDto;
import com.comeon.meetingservice.domain.meetingcode.service.MeetingCodeService;
import com.comeon.meetingservice.web.common.interceptor.MeetingAuth;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meetings/{meetingId}/codes")
@RequiredArgsConstructor
public class MeetingCodeController {

    private final MeetingCodeService meetingCodeService;

    @PatchMapping("/{codeId}")
    @MeetingAuth(meetingRoles = MeetingRole.HOST)
    public ApiResponse meetingCodeModify(@PathVariable("meetingId") Long meetingId,
                                         @PathVariable("codeId") Long id) {

        MeetingCodeModifyDto meetingCodeModifyDto = MeetingCodeModifyDto.builder()
                .meetingId(meetingId)
                .id(id)
                .build();

        meetingCodeService.modify(meetingCodeModifyDto);

        return ApiResponse.createSuccess();
    }
}
