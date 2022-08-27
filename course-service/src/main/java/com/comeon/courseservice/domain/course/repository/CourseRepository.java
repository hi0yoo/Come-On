package com.comeon.courseservice.domain.course.repository;

import com.comeon.courseservice.domain.course.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"coursePlaces"})
    @Query("select c from Course c where c.id = :courseId")
    Optional<Course> findByIdFetchCoursePlaces(@Param("courseId") Long courseId);
}
