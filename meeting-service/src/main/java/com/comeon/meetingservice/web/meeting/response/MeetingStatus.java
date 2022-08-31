package com.comeon.meetingservice.web.meeting.response;

import java.time.LocalDate;
import java.util.Objects;

public enum MeetingStatus {

    END,
    PROCEEDING,
    UNFIXED;

    public static MeetingStatus getMeetingStatus(LocalDate lastFixedDate) {
        if (Objects.isNull(lastFixedDate)) {
            return UNFIXED;
        } else if (lastFixedDate.isBefore(LocalDate.now())) {
            return END;
        } else {
            return PROCEEDING;
        }
    }
}
