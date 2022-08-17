package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;

public interface MeetingService {

    MeetingSaveDto add(MeetingSaveDto meetingSaveDto);

    MeetingModifyDto modify(MeetingModifyDto meetingSaveDto);

    MeetingRemoveDto remove(MeetingRemoveDto meetingRemoveDto);
}
