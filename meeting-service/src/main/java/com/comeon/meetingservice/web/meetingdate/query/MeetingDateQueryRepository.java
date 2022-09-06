package com.comeon.meetingservice.web.meetingdate.query;

import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.meetingservice.domain.meetingdate.entity.QDateUserEntity.*;
import static com.comeon.meetingservice.domain.meetingdate.entity.QMeetingDateEntity.*;
import static com.comeon.meetingservice.domain.meetinguser.entity.QMeetingUserEntity.*;

@Repository
@RequiredArgsConstructor
public class MeetingDateQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<MeetingDateEntity> findByIdFetchDateUser(Long meetingId, Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(meetingDateEntity)
                .join(meetingDateEntity.dateUserEntities, dateUserEntity).fetchJoin()
                .join(dateUserEntity.meetingUserEntity, meetingUserEntity).fetchJoin()
                .where(meetingDateEntity.meetingEntity.id.eq(meetingId),
                        meetingDateEntity.id.eq(id))
                .fetchOne());
    }
}
