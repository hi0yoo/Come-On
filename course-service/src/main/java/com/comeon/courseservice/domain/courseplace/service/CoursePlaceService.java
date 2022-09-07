package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CoursePlaceService {

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    // return savedCoursePlace.id
    public Long saveCoursePlace(Long courseId, Long userId, CoursePlaceDto coursePlaceDto) {
        Course course = getCourse(courseId);

        checkWriter(userId, course);

        Integer order = course.getCoursePlaces().stream()
                .mapToInt(CoursePlace::getOrder)
                .max()
                .orElse(0) + 1;
        coursePlaceDto.setOrder(order);

        CoursePlace coursePlace = coursePlaceDto.toEntity(course);

        return coursePlaceRepository.save(coursePlace).getId();
    }

    public void batchSaveCoursePlace(Long courseId, Long userId, List<CoursePlaceDto> coursePlaceDtoList) {
        Course course = getCourse(courseId);

        // 작성자 확인
        checkWriter(userId, course);

        // cascade.persist 이므로 트랜잭션 종료시, coursePlace 데이터 저장된다.
        coursePlaceDtoList.forEach(coursePlaceDto -> coursePlaceDto.toEntity(course));

        course.completeWriting();
    }


    /* === private method === */
    private Course getCourse(Long courseId) {
        return courseRepository.findByIdFetchCoursePlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId)
                );
    }

    private void checkWriter(Long userId, Course course) {
        if (!Objects.equals(course.getUserId(), userId)) {
            throw new CustomException("해당 코스에 장소를 등록 할 권한이 없습니다. 요청한 유저 식별값 : " + userId, ErrorCode.NO_AUTHORITIES);
        }
    }
}
