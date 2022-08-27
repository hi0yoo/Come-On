package com.comeon.courseservice.domain.course.service;

import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // 코스 저장
    public Long saveCourse(CourseDto courseDto) {
        return courseRepository.save(courseDto.toEntity()).getId();
    }

    // 코스 수정

    // 코스 삭제

}
