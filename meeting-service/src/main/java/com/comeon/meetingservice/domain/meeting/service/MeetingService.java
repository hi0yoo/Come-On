package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MeetingService {

    Long add(MeetingDto meetingDto) throws IOException;
}
