package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // 코스 저장
    public Long saveCourse(CourseDto courseDto) {
        // TODO 유저 정보 확인 필요?
        return courseRepository.save(courseDto.toEntity()).getId();
    }

    // 코스 작성 상태 수정
    public void completeWritingCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId)
                );

        if (!Objects.equals(course.getUserId(), userId)) {
            throw new CustomException("수정할 권한이 없습니다. 요청한 유저 식별값 : " + userId, ErrorCode.NO_AUTHORITIES);
        }

        if (!course.canCompleteWriting()) {
            throw new CustomException("코스 작성이 완료되지 않았습니다. 요청을 처리할 수 없습니다.", ErrorCode.VALIDATION_FAIL);
        }

        course.completeWriting();
    }

    // 코스 수정

    // 코스 삭제

}
