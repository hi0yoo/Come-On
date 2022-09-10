package com.comeon.courseservice.web.course.query.repository;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseLike;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.courseservice.domain.course.entity.QCourseLike.courseLike;

@Repository
@RequiredArgsConstructor
public class CourseLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<CourseLike> findByCourseAndUserId(Course course, Long userId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(courseLike)
                        .where(
                                courseLike.course.eq(course)
                                        .and(courseLike.userId.eq(userId))
                        )
                        .fetchOne()
        );
    }
}
