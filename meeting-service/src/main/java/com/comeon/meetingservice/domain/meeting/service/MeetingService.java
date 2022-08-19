package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;

public interface MeetingService {

    Long add(MeetingSaveDto meetingSaveDto);

    void modify(MeetingModifyDto meetingSaveDto);

    void remove(MeetingRemoveDto meetingRemoveDto);
}
