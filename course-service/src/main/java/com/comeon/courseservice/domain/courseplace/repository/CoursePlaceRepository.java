package com.comeon.courseservice.domain.courseplace.repository;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {
}
