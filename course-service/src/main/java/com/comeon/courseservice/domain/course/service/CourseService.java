package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseLikeRepository courseLikeRepository;

    // 코스 저장
    public Long saveCourse(CourseDto courseDto) {
        return courseRepository.save(courseDto.toEntity()).getId();
    }

    // 코스 수정
    public void modifyCourse(Long courseId, CourseDto courseDto) {
        // 코스와 코스 이미지 함께 조회
        Course course = courseRepository.findByIdFetchCourseImage(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

        // 작성자 확인
        checkWriter(courseDto.getUserId(), course);

        // 코스 정보 변경
        course.updateCourseInfo(courseDto.getTitle(), courseDto.getDescription());

        // 코스 이미지 데이터가 있으면 이미지 데이터 정보 변경
        CourseImageDto courseImageDto = courseDto.getCourseImageDto();
        if (Objects.nonNull(courseImageDto)) {
            course.getCourseImage().updateCourseImage(
                    courseImageDto.getOriginalName(),
                    courseImageDto.getStoredName()
            );
        }
    }

    // 코스 삭제
    public void removeCourse(Long courseId, Long userId) {
        // 코스 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

        // 작성자 확인
        checkWriter(userId, course);

        // 코스와 연관된 좋아요 전체 삭제
        courseLikeRepository.deleteByCourse(course);
        // 코스 삭제시 코스와 연관된 장소들, 이미지 함께 삭제(cascade)
        courseRepository.delete(course);
    }

    private void checkWriter(Long userId, Course course) {
        if (!course.getUserId().equals(userId)) {
            throw new CustomException("해당 코스의 작성자가 아니기 때문에 요청을 수행할 수 없습니다.", ErrorCode.NO_AUTHORITIES);
        }
    }
}
