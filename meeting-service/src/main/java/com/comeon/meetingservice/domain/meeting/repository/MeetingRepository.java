package com.comeon.meetingservice.domain.meeting.repository;

import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {

    @Query("select m from MeetingEntity m left join fetch m.meetingPlaceEntities where m.id = :id")
    Optional<MeetingEntity> findByIdFetchPlace(@Param("id") Long id);

    @Query("select m from MeetingEntity m join fetch m.meetingCodeEntity " +
            "where m.meetingCodeEntity.inviteCode = :inviteCode")
    Optional<MeetingEntity> findByInviteCodeFetchCode(@Param("inviteCode") String inviteCode);
}
