package com.comeon.meetingservice.domain.meetinguser.repository;

import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingUserRepository extends JpaRepository<MeetingUserEntity, Long> {

    @Query("select mu from MeetingUserEntity mu where mu.meetingEntity.id = :meetingId")
    List<MeetingUserEntity> findAllByMeetingId(@Param("meetingId") Long meetingId);
}
