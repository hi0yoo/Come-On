package com.comeon.meetingservice.domain.meeting.repository;

import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
}
