package com.comeon.courseservice.web.courseplace.query;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.QCoursePlace;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.courseservice.domain.course.entity.QCourse.course;
import static com.comeon.courseservice.domain.courseplace.entity.QCoursePlace.coursePlace;

@Repository
@RequiredArgsConstructor
public class CoursePlaceQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Course> findCourseByCourseIdFetchPlaces(Long courseId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(course)
                        .leftJoin(course.coursePlaces, coursePlace).fetchJoin()
                        .where(course.id.eq(courseId))
                        .orderBy(coursePlace.order.asc())
                        .fetchOne()
        );
    }

    public Optional<CoursePlace> findById(Long coursePlaceId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(coursePlace)
                        .where(coursePlace.id.eq(coursePlaceId))
                        .fetchOne()
        );
    }
}
