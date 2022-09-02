package com.comeon.meetingservice.web.meetingplace.query;

import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.meetingservice.domain.meetingplace.entity.QMeetingPlaceEntity.*;

@Repository
@RequiredArgsConstructor
public class MeetingPlaceQueryRepository {

    public final JPAQueryFactory queryFactory;

    public Optional<MeetingPlaceEntity> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(meetingPlaceEntity)
                .where(meetingPlaceEntity.id.eq(id))
                .fetchOne());
    }
}
