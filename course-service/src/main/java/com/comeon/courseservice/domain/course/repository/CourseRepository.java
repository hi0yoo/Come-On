package com.comeon.courseservice.domain.course.repository;

import com.comeon.courseservice.domain.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
