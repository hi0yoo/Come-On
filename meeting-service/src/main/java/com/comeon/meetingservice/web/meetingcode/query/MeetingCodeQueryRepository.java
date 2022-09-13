package com.comeon.meetingservice.web.meetingcode.query;

import com.comeon.meetingservice.domain.meeting.entity.QMeetingEntity;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.meetingservice.domain.meeting.entity.QMeetingEntity.*;
import static com.comeon.meetingservice.domain.meetingcode.entity.QMeetingCodeEntity.*;

@Repository
@RequiredArgsConstructor
public class MeetingCodeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<MeetingCodeEntity> findById(Long meetingId, Long id) {
        return Optional.ofNullable(queryFactory
                        .select(meetingCodeEntity)
                        .from(meetingEntity)
                        .join(meetingEntity.meetingCodeEntity, meetingCodeEntity)
                        .where(meetingEntity.id.eq(meetingId),
                                meetingCodeEntity.id.eq(id))
                        .fetchOne());
    }

}
