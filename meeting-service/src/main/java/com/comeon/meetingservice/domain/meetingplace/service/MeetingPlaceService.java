package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceRemoveDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;

public interface MeetingPlaceService {

    Long add(MeetingPlaceAddDto meetingPlaceAddDto);

    void modify(MeetingPlaceModifyDto meetingPlaceModifyDto);

    void remove(MeetingPlaceRemoveDto meetingPlaceRemoveDto);
}
