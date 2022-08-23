package com.comeon.meetingservice.domain.meetinguser.repository;

import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingUserRepository extends JpaRepository<MeetingUserEntity, Long> {

    @Query("select mu from MeetingUserEntity mu where mu.meetingEntity.id = :meetingId")
    List<MeetingUserEntity> findAllByMeetingId(@Param("meetingId") Long meetingId);

    @Query("select mu from MeetingUserEntity mu " +
            "where mu.userId = :userId and mu.meetingEntity.id = :meetingId")
    Optional<MeetingUserEntity> findByUserIdAndMeetingId(@Param("userId") Long userId,
                                                         @Param("meetingId") Long meetingId);
}
