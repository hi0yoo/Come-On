package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;

public interface MeetingService {

    Long add(MeetingAddDto meetingAddDto);

    void modify(MeetingModifyDto meetingSaveDto);

    void remove(MeetingRemoveDto meetingRemoveDto);
}
