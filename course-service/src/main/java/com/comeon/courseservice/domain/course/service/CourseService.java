package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseLikeRepository courseLikeRepository;

    // 코스 저장
    public Long saveCourse(CourseDto courseDto) {
        // TODO 유저 정보 확인 필요?
        return courseRepository.save(courseDto.toEntity()).getId();
    }

    // 코스 수정

    // 코스 삭제
    public void removeCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

        if (!course.getUserId().equals(userId)) {
            throw new CustomException("해당 코스를 등록한 유저가 아닙니다.", ErrorCode.NO_AUTHORITIES);
        }

        // 코스와 연관된 좋아요 전체 삭제
        courseLikeRepository.deleteByCourse(course);
        courseRepository.delete(course);
    }
}
