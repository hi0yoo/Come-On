package com.comeon.meetingservice.web.meeting.query;

import com.comeon.meetingservice.web.meeting.query.dto.MeetingCondition;
import com.comeon.meetingservice.web.meeting.query.dto.MeetingQueryListDto;
import com.querydsl.core.types.Projections;
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

import static com.comeon.meetingservice.domain.meeting.entity.QMeetingEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingFileEntity.*;
import static com.comeon.meetingservice.domain.meeting.entity.QMeetingUserEntity.*;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Slice<MeetingQueryListDto> findSliceByUserId(Long userId, Pageable pageable, MeetingCondition meetingCondition) {
        List<MeetingQueryListDto> meetingQueryListDtos = queryFactory
                .select(Projections.bean(MeetingQueryListDto.class,
                        meetingEntity.id,
                        meetingEntity.title,
                        meetingEntity.startDate,
                        meetingEntity.endDate,
                        meetingEntity.meetingFileEntity.storedName,
                        meetingEntity.meetingCodeEntity.id.as("meetingCodeId")))
                .from(meetingEntity)
                .join(meetingEntity.meetingFileEntity, meetingFileEntity)
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

        return new SliceImpl<>(meetingQueryListDtos, pageable,
                calculateHasNext(pageable, meetingQueryListDtos));
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
