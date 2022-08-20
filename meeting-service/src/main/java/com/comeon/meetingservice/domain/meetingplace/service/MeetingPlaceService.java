package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceSaveDto;

public interface MeetingPlaceService {

    Long add(MeetingPlaceSaveDto meetingPlaceSaveDto);

    void modify(MeetingPlaceModifyDto meetingPlaceModifyDto);
}
