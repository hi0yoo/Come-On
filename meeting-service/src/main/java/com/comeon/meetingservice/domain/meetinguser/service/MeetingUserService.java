package com.comeon.meetingservice.domain.meetinguser.service;

import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;

public interface MeetingUserService {

    Long add(MeetingUserAddDto meetingUserAddDto);
}
