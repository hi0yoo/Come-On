package com.comeon.meetingservice.web.meeting.query;

import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.comeon.meetingservice.domain.meeting.entity.QMeetingDateEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingFileEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingPlaceEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingUserEntity.*;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<MeetingEntity> findSliceByUserId(Long userId,
                                                  Pageable pageable,
                                                  MeetingCondition meetingCondition) {

        List<MeetingEntity> meetingEntities = queryFactory
                .selectFrom(meetingEntity).distinct()
                .join(meetingEntity.meetingFileEntity, meetingFileEntity).fetchJoin()
                .join(meetingEntity.meetingUserEntities, meetingUserEntity)
                .where(meetingUserEntity.userId.eq(userId),
                        titleContains(meetingCondition.getTitle()),
                        startDateAfter(meetingCondition.getStartDate()),
                        endDateBefore(meetingCondition.getEndDate()))
                .orderBy(meetingEntity.startDate.desc(),
                        meetingEntity.createdDateTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new SliceImpl<>(meetingEntities, pageable,
                calculateHasNext(pageable, meetingEntities));
    }

    public Optional<MeetingEntity> findById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(meetingEntity)
                .leftJoin(meetingEntity.meetingPlaceEntities, meetingPlaceEntity).fetchJoin()
                .leftJoin(meetingEntity.meetingDateEntities, meetingDateEntity).fetchJoin()
                .join(meetingEntity.meetingUserEntities, meetingUserEntity).fetchJoin()
                .where(meetingEntity.id.eq(id))
                .fetchOne());
    }

    public Optional<String> findStoredNameById(Long id) {
        return Optional.ofNullable(queryFactory
                .select(meetingFileEntity.storedName)
                .from(meetingEntity)
                .join(meetingEntity.meetingFileEntity, meetingFileEntity)
                .where(meetingEntity.id.eq(id))
                .fetchOne());
    }

    private boolean calculateHasNext(Pageable pageable, List<?> contents) {
        boolean hasNext = false;

        if (contents.size() > pageable.getPageSize()) {
            contents.remove(pageable.getPageSize());
            hasNext = true;
        }

        return hasNext;
    }

    private BooleanExpression titleContains(String title) {
        return Objects.isNull(title) ?
                null : meetingEntity.title.containsIgnoreCase(title);
    }

    private BooleanExpression startDateAfter(LocalDate afterDate) {
        return Objects.isNull(afterDate) ?
                null : meetingEntity.startDate.goe(afterDate);
    }

    private BooleanExpression endDateBefore(LocalDate endDate) {
        return Objects.isNull(endDate) ?
                null : meetingEntity.endDate.loe(endDate);
    }

}
