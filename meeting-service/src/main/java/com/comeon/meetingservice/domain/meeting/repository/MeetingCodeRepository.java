package com.comeon.meetingservice.domain.meeting.repository;

import com.comeon.meetingservice.domain.meeting.entity.MeetingCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingCodeRepository extends JpaRepository<MeetingCodeEntity, Long> {

    @Query("select mc from MeetingCodeEntity mc where mc.inviteCode = :inviteCode")
    Optional<MeetingCodeEntity> findByInviteCode(@Param("inviteCode") String inviteCode);
}
