package com.comeon.meetingservice.domain.meetingcode.repository;

import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingCodeRepository extends JpaRepository<MeetingCodeEntity, Long> {

    @Query("select mc from MeetingCodeEntity mc where mc.inviteCode = :inviteCode")
    Optional<MeetingCodeEntity> findByInviteCode(@Param("inviteCode") String inviteCode);

    @Query("select mc from MeetingEntity m join m.meetingCodeEntity mc " +
            "where m.id = :meetingId " +
            "and mc.id = :id")
    Optional<MeetingCodeEntity> findById(@Param("meetingId") Long meetingId, @Param("id") Long id);
}
