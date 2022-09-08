package com.comeon.courseservice.web.course.query.repository;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseWriteStatus;
import com.comeon.courseservice.web.course.query.CourseCondition;
import com.comeon.courseservice.web.course.query.repository.dto.CourseListData;
import com.comeon.courseservice.web.course.query.repository.dto.MyPageCourseListData;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.comeon.courseservice.domain.course.entity.QCourse.course;
import static com.comeon.courseservice.domain.course.entity.QCourseImage.courseImage;
import static com.comeon.courseservice.domain.courselike.entity.QCourseLike.courseLike;
import static com.comeon.courseservice.domain.courseplace.entity.QCoursePlace.coursePlace;
import static com.querydsl.core.types.dsl.Expressions.*;
import static com.querydsl.core.types.dsl.MathExpressions.*;

@Repository
@RequiredArgsConstructor
public class CourseQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<Course> findByIdFetchAll(Long courseId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(course)
                        .leftJoin(course.courseImage, courseImage).fetchJoin()
                        .leftJoin(course.coursePlaces, coursePlace).fetchJoin()
                        .where(course.id.eq(courseId))
                        .orderBy(coursePlace.order.asc())
                        .fetchOne()
        );
    }

    public Optional<Course> findByIdFetchCourseImg(Long courseId) {
        return Optional.ofNullable(
                queryFactory.selectFrom(course)
                        .join(course.courseImage, courseImage).fetchJoin()
                        .where(course.id.eq(courseId))
                        .fetchOne()
        );
    }

    /*
    정렬 - 위치 가까운 순(o), 좋아요 많은 순(o), 최신순(0)
    검색 조건 - 코스 제목(o) 검색

    - 코스에 등록된 첫번째 장소를 가져와야 한다.
    - 코스에 등록된 현재 유저의 좋아요를 가져와야 한다.
    - 코스 첫번째 장소와 현재 위치의 거리를 계산하여, asc로 정렬해야 한다.
     */
    public Slice<CourseListData> findCourseSlice(Long userId,
                                                 CourseCondition courseCondition,
                                                 Pageable pageable) {
        // TODO 수정
        Double lat = Objects.isNull(courseCondition.getLat()) ? Double.valueOf(37.555945) : courseCondition.getLat();
        Double lng = Objects.isNull(courseCondition.getLng()) ? Double.valueOf(126.972331) : courseCondition.getLng();
        Expression<Double> userLat = constant(lat);
        Expression<Double> userLng = constant(lng);

        // 현재 위치와 코스 첫번째 장소 사이의 거리 구하는 서브쿼리
        JPQLQuery<Double> distanceSubQuery = JPAExpressions
                .select(
                        acos(
                                cos(radians(userLat))
                                        .multiply(cos(radians(coursePlace.lat)))
                                        .multiply(cos(radians(coursePlace.lng)
                                                .subtract(radians(userLng)))
                                        )
                                        .add(sin(radians(userLat))
                                                .multiply(sin(radians(coursePlace.lat)))
                                        )
                        ).multiply(constant(6371)))
                .from(coursePlace)
                .where(coursePlace.course.eq(course),
                        coursePlace.order.eq(1)
                );

        // 결과로 내려주기 위한 컬럼명
        String distanceFieldName = "distance";

        List<CourseListData> courseListDatas = queryFactory
                .select(Projections.constructor(CourseListData.class,
                                course,
                                coursePlace,
                                ExpressionUtils.as(distanceSubQuery, distanceFieldName),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(courseLike.id)
                                                .from(courseLike)
                                                .where(courseLike.course.eq(course),
//                                                        courseLike.userId.eq(userId)
                                                        userIdEq(userId)
                                                ), "courseLikeId")
                        )
                ).from(course)
                .leftJoin(course.courseImage, courseImage).fetchJoin()
                .leftJoin(coursePlace).on(coursePlace.course.eq(course))
                .where(
                        coursePlace.order.eq(1), // 코스의 첫번째 장소만 가져온다.
                        course.writeStatus.eq(CourseWriteStatus.COMPLETE), // 작성 완료된 코스만 가져온다.,
                        titleContains(courseCondition.getTitle()),
                        distanceSubQuery.loe(Double.valueOf(100)) // 100km 이내
                )
                .orderBy(
                        numberPath(Double.class, distanceFieldName).asc(), // 거리 컬럼을 오름차순 정렬
                        course.likeCount.desc(),
                        course.lastModifiedDate.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new SliceImpl<>(courseListDatas, pageable, hasNext(pageable, courseListDatas));
    }

    // 사용자가 등록한 코스 리스트 조회
    public Slice<MyPageCourseListData> findMyCourseSlice(Long userId,
                                                         Pageable pageable) {
        List<MyPageCourseListData> myPageCourseList = queryFactory
                .select(Projections.constructor(MyPageCourseListData.class,
                        course,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(courseLike.id)
                                        .from(courseLike)
                                        .where(courseLike.course.eq(course),
                                                courseLike.userId.eq(userId)
                                        ), "courseLikeId")))
                .from(course)
                .leftJoin(course.courseImage, courseImage).fetchJoin()
                .where(
                        course.userId.eq(userId),
                        course.writeStatus.eq(CourseWriteStatus.COMPLETE) // 작성 완료된 코스만 가져온다.,
                )
                .orderBy(
                        course.lastModifiedDate.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new SliceImpl<>(myPageCourseList, pageable, hasNext(pageable, myPageCourseList));
    }

    // 사용자가 좋아요한 코스 리스트 조회
    // TODO 수정
    public Slice<MyPageCourseListData> findMyLikedCourseSlice(Long userId,
                                                              Pageable pageable) {
        List<MyPageCourseListData> myPageCourseList = queryFactory
                .select(Projections.constructor(MyPageCourseListData.class,
                        course,
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(courseLike.id)
                                        .from(courseLike)
                                        .where(courseLike.course.eq(course),
                                                courseLike.userId.eq(userId)
                                        ), "courseLikeId")))
                .from(course)
                .leftJoin(course.courseImage, courseImage).fetchJoin()
                .leftJoin(courseLike).on(courseLike.course.eq(course))
                .where(
                        courseLike.userId.eq(userId),
                        course.writeStatus.eq(CourseWriteStatus.COMPLETE) // 작성 완료된 코스만 가져온다.,
                )
                .orderBy(
                        courseLike.lastModifiedDate.desc() // 좋아요 등록일 최신순 정렬
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return new SliceImpl<>(myPageCourseList, pageable, hasNext(pageable, myPageCourseList));
    }

    private boolean hasNext(Pageable pageable, List<?> contents) {
        if (contents.size() > pageable.getPageSize()) {
            contents.remove(pageable.getPageSize());
            return true;
        }
        return false;
    }

    // TODO 검색 로직 최적화
    private BooleanExpression titleContains(String title) {
        return Objects.isNull(title) ?
                null : course.title.containsIgnoreCase(title);
    }

    private BooleanExpression userIdEq(Long userId) {
        return Objects.isNull(userId) ?
                courseLike.userId.isNull() : courseLike.userId.eq(userId);
    }
}
