package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CoursePlaceService {

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public Long saveCoursePlace(Long courseId, CoursePlaceDto coursePlaceDto) {
        Course course = courseRepository.findByIdFetchCoursePlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId)
                );

        Integer order = course.getCoursePlaces().stream()
                .mapToInt(CoursePlace::getOrder)
                .max()
                .orElse(0) + 1;

        CoursePlace coursePlace = coursePlaceDto.toEntity(course, order);

        return coursePlaceRepository.save(coursePlace).getId();
    }
}
