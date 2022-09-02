package com.comeon.meetingservice.domain.meetingdate.repository;

import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MeetingDateRepository extends JpaRepository<MeetingDateEntity, Long> {

    @Modifying
    @Query("delete from MeetingDateEntity md " +
            "where md.meetingEntity.id = :meetingId " +
            "and md.date not between :startDate and :endDate")
    void deleteIfNotBetweenDate(@Param("meetingId")Long meetingId,
                                @Param("startDate")LocalDate startDate,
                                @Param("endDate")LocalDate endDate);

    @Query("select md from MeetingDateEntity md " +
            "where md.meetingEntity.id = :meetingId and md.date = :date")
    Optional<MeetingDateEntity> findByMeetingIdAndDate(@Param("meetingId") Long meetingId,
                                                       @Param("date") LocalDate date);
}
