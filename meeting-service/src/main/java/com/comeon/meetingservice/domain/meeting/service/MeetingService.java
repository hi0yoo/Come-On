package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;

public interface MeetingService {

    Long add(MeetingDto meetingDto);

    MeetingDto modify(MeetingDto meetingDto);
}
