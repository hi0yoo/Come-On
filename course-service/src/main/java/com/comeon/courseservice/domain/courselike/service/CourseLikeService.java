package com.comeon.courseservice.domain.courselike.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseLikeService {

    private final CourseRepository courseRepository;
    private final CourseLikeRepository courseLikeRepository;

    public Long updateCourseLike(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId).orElseThrow(
                () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
        );

        Optional<CourseLike> optionalCourseLike =
                courseLikeRepository.findByCourseIdAndUserIdFetchCourse(courseId, userId);

        if (optionalCourseLike.isEmpty()) { // 등록된 좋아요가 없는 경우
            // 작성이 완료되지 않은 코스에는 좋아요를 등록할 수 없다.
            if (!course.isWritingComplete()) {
                throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + course.getId(), ErrorCode.CAN_NOT_ACCESS_RESOURCE);
            }

            // 생성하고 저장
            return saveCourseLike(
                    CourseLike.builder()
                            .course(course)
                            .userId(userId)
                            .build()
            );
        } else { // 등록된 좋아요가 있는 경우
            // 좋아요가 등록된 코스의 count 1 감소
            removeCourseLike(optionalCourseLike.get());
            return null;
        }
    }

    private Long saveCourseLike(CourseLike courseLike) {
        return courseLikeRepository.save(courseLike).getId();
    }

    private void removeCourseLike(CourseLike courseLike) {
        // 좋아요가 등록된 코스의 count 1 감소
        courseLike.getCourse().decreaseLikeCount();
        courseLikeRepository.delete(courseLike);
    }
}
