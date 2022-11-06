package com.comeon.courseservice.domain.courseplace.repository;

import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    @EntityGraph(attributePaths = "course")
    @Query("select cp from CoursePlace cp " +
            "where cp.id = :coursePlaceId")
    Optional<CoursePlace> findByIdFetchCourse(@Param("coursePlaceId") Long coursePlaceId);

    List<CoursePlace> findAllByCourseId(Long courseId);

    Optional<CoursePlace> findByCourseIdAndOrder(Long courseId, Integer order);
}
