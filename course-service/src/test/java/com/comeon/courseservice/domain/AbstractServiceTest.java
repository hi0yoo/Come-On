package com.comeon.courseservice.domain;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractServiceTest {

    private AtomicLong courseIdGenerater = new AtomicLong();
    private AtomicLong coursePlaceIdGenerater = new AtomicLong();
    private AtomicLong courseLikeIdGenerater = new AtomicLong();

    private List<Course> courseList = new ArrayList<>();
    private List<CourseLike> courseLikeList = new ArrayList<>();

    public List<Course> getCourseList() {
        return courseList;
    }

    public List<CourseLike> getCourseLikeList() {
        return courseLikeList;
    }

    public List<Course> setCourses(Long userId, int count) {
        List<Course> courseList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            LocalDateTime randomDate = randomDate();
            long courseId = this.courseIdGenerater.incrementAndGet();

            Course course = Course.builder()
                    .userId(userId)
                    .title("courseTitle" + courseId)
                    .description("courseTitle" + courseId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("originalFileName" + courseId)
                                    .storedName("storderFileName" + courseId)
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
            long coursePlaceId = this.coursePlaceIdGenerater.incrementAndGet();
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
        course.writeComplete();
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
                            ReflectionTestUtils.setField(courseLike, "id", courseLikeIdGenerater.incrementAndGet());
                            int randomHours = nextInt(30);
                            ReflectionTestUtils.setField(courseLike, "createdDate", course.getCreatedDate().plusHours(randomHours));
                            ReflectionTestUtils.setField(courseLike, "lastModifiedDate", course.getLastModifiedDate().plusHours(randomHours));
                            courseLikeList.add(courseLike);
                        }
                );
    }


    private LocalDateTime randomDate() {
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        return now.minusDays(random.nextInt(10));
    }
}
