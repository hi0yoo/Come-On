package com.comeon.meetingservice.domain.meetingplace.repository;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingPlaceRepository extends JpaRepository<MeetingPlaceEntity, Long> {
}
