package com.comeon.meetingservice.domain.meetingdate.service;

import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateRemoveDto;

public interface MeetingDateService {

    Long add(MeetingDateAddDto meetingDateAddDto);

    void modify(MeetingDateModifyDto meetingDateModifyDto);

    void remove(MeetingDateRemoveDto meetingDateRemoveDto);
}
