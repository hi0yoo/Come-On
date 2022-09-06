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

//    // 코스 좋아요 등록
//    public Long saveCourseLike(Long courseId, Long userId) {
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(
//                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
//                );
//
//        // 작성 완료되지 않은 코스는 조회 X
//        if (!course.isWritingComplete()) {
//            throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.CAN_NOT_ACCESS_RESOURCE);
//        }
//
//        courseLikeRepository.findByCourseAndUserIdFetchCourse(course, userId)
//                .ifPresent(courseLike -> {
//                    throw new CustomException("이미 좋아요 처리되었습니다. 좋아요 식별값 : " + courseLike.getId(), ErrorCode.ALREADY_EXIST);
//                });
//
//        CourseLike courseLike = CourseLike.builder()
//                .course(course)
//                .userId(userId)
//                .build();
//
//        return courseLikeRepository.save(courseLike).getId();
//    }
//
//    // 코스 좋아요 삭제
//    public void removeCourseLike(Long courseLikeId, Long courseId, Long userId) {
//        CourseLike courseLike = courseLikeRepository.findByIdFetch(courseLikeId)
//                .orElseThrow(
//                        () -> new EntityNotFoundException("해당 식별값의 좋아요가 존재하지 않습니다. 요청한 좋아요 식별값 : " + courseId)
//                );
//
//        if (!courseLike.getCourse().getId().equals(courseId)) {
//            throw new CustomException("좋아요가 등록된 코스가 일치하지 않습니다.", ErrorCode.VALIDATION_FAIL);
//        }
//
//        if (!courseLike.getUserId().equals(userId)) {
//            throw new CustomException("해당 좋아요를 등록한 유저가 아닙니다. 요청한 유저 : " + userId, ErrorCode.NO_AUTHORITIES);
//        }
//
//        courseLike.getCourse().decreaseLikeCount();
//        courseLikeRepository.delete(courseLike);
//    }
}
