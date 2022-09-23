package com.comeon.meetingservice.domain.meetinguser.service;

import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserModifyDto;

public interface MeetingUserService {

    Long add(MeetingUserAddDto meetingUserAddDto);

    void modify(MeetingUserModifyDto meetingUserModifyDto);
}
