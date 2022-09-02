package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.domain.course.entity.Course;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.comeon.courseservice.domain.course.entity.QCourse.course;
import static com.comeon.courseservice.domain.course.entity.QCourseImage.courseImage;
import static com.comeon.courseservice.domain.courseplace.entity.QCoursePlace.coursePlace;

@Repository
@RequiredArgsConstructor
public class CourseQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Course> findById(Long courseId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(course)
                        .leftJoin(course.courseImage, courseImage).fetchJoin()
                        .leftJoin(course.coursePlaces, coursePlace).fetchJoin()
                        .where(course.id.eq(courseId))
                        .orderBy(coursePlace.order.asc())
                        .fetchOne()
        );
    }
}
