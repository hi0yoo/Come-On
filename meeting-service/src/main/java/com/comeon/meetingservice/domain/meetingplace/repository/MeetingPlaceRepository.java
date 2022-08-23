package com.comeon.meetingservice.domain.meetingplace.repository;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingPlaceRepository extends JpaRepository<MeetingPlaceEntity, Long> {

    @Query("select mp from MeetingPlaceEntity mp where mp.meetingEntity.id = :meetingId")
    List<MeetingPlaceEntity> findAllByMeetingId(@Param("meetingId") Long meetingId);
}
