package com.comeon.meetingservice.domain.meetingdate.service;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;

public interface MeetingDateService {

    Long add(MeetingDateAddDto meetingDateAddDto);
}
