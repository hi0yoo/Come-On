//package com.comeon.courseservice.domain.course.repository;
//
//import com.comeon.courseservice.domain.course.entity.Course;
//import com.comeon.courseservice.domain.course.entity.CourseLike;
//import org.springframework.data.jpa.repository.EntityGraph;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Optional;
//
//public interface CourseLikeRepository extends JpaRepository<CourseLike, Long> {
//
//    @EntityGraph(attributePaths = "course")
//    @Query("select cl from CourseLike cl " +
//            "where cl.id = :courseId")
//    Optional<CourseLike> findByIdFetch(@Param("courseId") Long courseId);
//
//    @EntityGraph(attributePaths = "course")
//    @Query("select cl from CourseLike cl " +
//            "where cl.course = :course and cl.userId = :userId")
//    Optional<CourseLike> findByCourseAndUserIdFetchCourse(@Param("course") Course course,
//                                                          @Param("userId") Long userId);
//}
