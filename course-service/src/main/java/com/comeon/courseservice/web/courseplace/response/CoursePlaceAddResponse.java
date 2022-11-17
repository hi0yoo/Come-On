package com.comeon.courseservice.web.courseplace.response;

import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CoursePlaceAddResponse {

    private Long targetCourseId;
    private CourseStatus courseStatus;
    private AddedCoursePlaceInfo addedCoursePlaceInfo;
    private List<CoursePlaceDetails> coursePlaces;

    public CoursePlaceAddResponse(Course course, Long addedCoursePlaceId) {
        this.targetCourseId = course.getId();
        this.courseStatus = course.getCourseStatus();
        List<CoursePlace> places = course.getCoursePlaces();
        this.addedCoursePlaceInfo = new AddedCoursePlaceInfo(
                places.stream()
                        .filter(coursePlace -> coursePlace.getId().equals(addedCoursePlaceId))
                        .findFirst()
                        .orElseThrow()
        );
        this.coursePlaces = places.stream()
                .map(CoursePlaceDetails::new)
                .collect(Collectors.toList());
    }

    @Getter
    private static class AddedCoursePlaceInfo {
        private Long coursePlaceId;
        private Integer coursePlaceOrder;

        public AddedCoursePlaceInfo(CoursePlace coursePlace) {
            this.coursePlaceId = coursePlace.getId();
            this.coursePlaceOrder = coursePlace.getOrder();
        }
    }
}
