package com.comeon.courseservice.utils;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;

public class UnitTestDataManager {

    private AtomicLong courseId = new AtomicLong();
    private AtomicLong coursePlaceId = new AtomicLong();
    private AtomicLong courseLikeId = new AtomicLong();

    private List<Course> courseList = new ArrayList<>();
    private List<CourseLike> courseLikeList = new ArrayList<>();

    public List<Course> getCourseList() {
        return courseList;
    }

    public List<CourseLike> getCourseLikeList() {
        return courseLikeList;
    }

    public void initData() {
        courseList = initCourse(1L, 15);
        courseList.addAll(initCourse(2L, 15));
        courseList.addAll(initCourse(3L, 15));

        for (Course course : courseList) {
            setCoursePlaces(course, 5);
        }

        Random random = new Random();
        for (Course course : courseList) {
            for (int i = 0; i < 5; i++) {
                setCourseLike(course, (long) (random.nextInt(10) + 1));
            }
        }
    }

    private LocalDateTime randomDate() {
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        return now.minusDays(random.nextInt(10));
    }

    public List<Course> initCourse(Long userId, int count) {
        List<Course> courseList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            LocalDateTime randomDate = randomDate();
            long courseId = this.courseId.incrementAndGet();
            Course course = Course.builder()
                    .userId(userId)
                    .title("courseTitle" + courseId)
                    .description("courseTitle" + courseId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("originalName" + courseId)
                                    .storedName("storedName" + courseId)
                                    .build()
                    )
                    .build();
            ReflectionTestUtils.setField(course, "id", courseId);
            ReflectionTestUtils.setField(course, "createdDate", randomDate);
            ReflectionTestUtils.setField(course, "lastModifiedDate", randomDate);
            courseList.add(course);
        }

        return courseList;
    }

    public void setCoursePlaces(Course course, int count) {
        int size = course.getCoursePlaces().size();
        for (int i = size + 1; i <= size + count; i++) {
            long coursePlaceId = this.coursePlaceId.incrementAndGet();
            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name("placeName" + coursePlaceId)
                    .description("placeDescription" + coursePlaceId)
                    .lat(nextDouble() * (38 - 36 + 1) + 36)
                    .lng(nextDouble() * (128 - 126 + 1) + 126)
                    .order(i)
                    .kakaoPlaceId(coursePlaceId)
                    .placeCategory(CoursePlaceCategory.ETC)
                    .build();
            ReflectionTestUtils.setField(coursePlace, "id", coursePlaceId);
            ReflectionTestUtils.setField(coursePlace, "createdDate", course.getCreatedDate());
            ReflectionTestUtils.setField(coursePlace, "lastModifiedDate", course.getLastModifiedDate());
        }
        course.completeWriting();
    }

    public void setCourseLike(Course course, Long userId) {
        courseLikeList.stream()
                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(userId))
                .findFirst()
                .ifPresentOrElse(
                        courseLikeList::remove,
                        () -> {
                            CourseLike courseLike = CourseLike.builder()
                                    .course(course)
                                    .userId(userId)
                                    .build();
                            ReflectionTestUtils.setField(courseLike, "id", courseLikeId.incrementAndGet());
                            int randomHours = nextInt(30);
                            ReflectionTestUtils.setField(courseLike, "createdDate", course.getCreatedDate().plusHours(randomHours));
                            ReflectionTestUtils.setField(courseLike, "lastModifiedDate", course.getLastModifiedDate().plusHours(randomHours));
                            courseLikeList.add(courseLike);
                        }
                );
    }
}
