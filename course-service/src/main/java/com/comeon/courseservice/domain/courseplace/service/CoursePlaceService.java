package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.repository.CoursePlaceRepository;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CoursePlaceService {

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public Long coursePlaceAdd(Long courseId, Long userId, CoursePlaceDto coursePlaceDto) {
        Course course = getCourse(courseId);
        checkWriter(userId, course);

        coursePlaceDto.setOrder(course.getCoursePlaces().size() + 1);
        Long coursePlaceId = coursePlaceRepository.save(coursePlaceDto.toEntity(course)).getId();

        course.availableCourse();

        return coursePlaceId;
    }

    public void coursePlaceModify(Long courseId, Long userId, Long coursePlaceId, CoursePlaceDto coursePlaceDto) {
        CoursePlace coursePlace = findCoursePlace(coursePlaceId, courseId, userId);

        if (coursePlaceDto.getDescription() != null) {
            coursePlace.updateDescription(coursePlace.getDescription());
        }
        if (coursePlaceDto.getPlaceCategory() != null) {
            coursePlace.updatePlaceCategory(coursePlaceDto.getPlaceCategory());
        }

        if (coursePlaceDto.getOrder() != null) {
            Integer originalOrder = coursePlace.getOrder();
            Integer targetOrder = coursePlaceDto.getOrder();

            CoursePlace targetPlace = coursePlaceRepository.findByCourseIdAndOrder(courseId, targetOrder)
                    .orElseThrow(
                            () -> new CustomException("코스에 " + targetOrder + "번 순서의 장소가 없습니다.", ErrorCode.NOT_EXIST_PLACE_ORDER)
                    );

            coursePlace.updateOrder(targetOrder);
            targetPlace.updateOrder(originalOrder);
        }
    }

    public void coursePlaceRemove(Long courseId, Long userId, Long coursePlaceId) {
        CoursePlace coursePlace = findCoursePlace(coursePlaceId, courseId, userId);

        List<CoursePlace> coursePlaces = coursePlaceRepository.findAllByCourseId(courseId);
        coursePlaces.remove(coursePlace);

        decreaseAfterOrder(coursePlaces, coursePlace.getOrder());

        if (coursePlaces.size() == 0) {
            coursePlace.getCourse().disabledCourse();
        }
    }

    private CoursePlace findCoursePlace(Long coursePlaceId, Long courseId, Long userId) {
        CoursePlace coursePlace = coursePlaceRepository.findByIdFetchCourse(coursePlaceId)
                .orElseThrow(
                        () -> new EntityNotFoundException("코스 장소가 없습니다. 요청한 코스 식별값 : " + courseId + ", 요청한 장소 식별값 : " + coursePlaceId)
                );
        checkWriter(userId, coursePlace.getCourse());
        return coursePlace;
    }

    private void decreaseAfterOrder(List<CoursePlace> coursePlaces, Integer deletedOrder) {
        coursePlaces.stream()
                .filter(coursePlace -> coursePlace.getOrder() > deletedOrder)
                .forEach(CoursePlace::decreaseOrder);
    }

    public void batchUpdateCoursePlace(Long courseId, Long userId,
                                       List<CoursePlaceDto> dtosToSave,
                                       List<CoursePlaceDto> dtosToModify,
                                       List<Long> coursePlaceIdsToDelete) {

        Course course = getCourse(courseId);

        checkWriter(userId, course);

        // 삭제
        coursePlaceIdsToDelete.forEach(
                coursePlaceId -> course.getCoursePlaces().removeIf(coursePlace -> coursePlace.getId().equals(coursePlaceId))
        );

        // 수정
        dtosToModify.forEach(
                coursePlaceDto -> course.getCoursePlaces().stream()
                        .filter(coursePlace -> coursePlace.getId().equals(coursePlaceDto.getCoursePlaceId()))
                        .findFirst()
                        .ifPresent(coursePlace -> modify(coursePlace, coursePlaceDto))
        );

        // 등록
        dtosToSave.forEach(coursePlaceDto -> coursePlaceDto.toEntity(course));

        // 순서 체크
        checkPlaceOrders(course);

        course.updateCourseState();
    }


    /* === private method === */
    private Course getCourse(Long courseId) {
        return courseRepository.findByIdFetchCoursePlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId)
                );
    }

    private void checkWriter(Long userId, Course course) {
        if (!Objects.equals(course.getUserId(), userId)) {
            throw new CustomException("해당 코스의 작성자가 아닙니다. 요청한 유저 식별값 : " + userId, ErrorCode.NO_AUTHORITIES);
        }
    }

    private void checkPlaceOrders(Course course) {
        List<Integer> orderList = course.getCoursePlaces().stream()
                .map(CoursePlace::getOrder)
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        if (orderList.size() != course.getCoursePlaces().size()) {
            throw new CustomException("장소의 순서가 중복되었습니다.", ErrorCode.PLACE_ORDER_DUPLICATE);
        }
        if (!orderList.get(0).equals(1)) {
            throw new CustomException("장소의 순서가 1부터 시작하지 않습니다.", ErrorCode.PLACE_ORDER_NOT_START_ONE);
        }
        for (int i = 0; i < orderList.size() - 1; i++) {
            if (orderList.get(i) + 1 != orderList.get(i + 1)) {
                throw new CustomException("장소의 순서가 연속적인 값들이 아닙니다.", ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE);
            }
        }
    }

    private void modify(CoursePlace coursePlace, CoursePlaceDto coursePlaceDto) {
        if (Objects.nonNull(coursePlaceDto.getName())) {
            coursePlace.updateName(coursePlaceDto.getName());
        }

        if (Objects.nonNull(coursePlaceDto.getDescription())) {
            coursePlace.updateDescription(coursePlaceDto.getDescription());
        }

        if (Objects.nonNull(coursePlaceDto.getLat())) {
            coursePlace.updateLat(coursePlaceDto.getLat());
        }

        if (Objects.nonNull(coursePlaceDto.getLng())) {
            coursePlace.updateLng(coursePlaceDto.getLng());
        }

        if (Objects.nonNull(coursePlaceDto.getOrder())) {
            coursePlace.updateOrder(coursePlaceDto.getOrder());
        }

        if (Objects.nonNull(coursePlaceDto.getKakaoPlaceId())) {
            coursePlace.updateKakaoPlaceId(coursePlaceDto.getKakaoPlaceId());
        }

        if (Objects.nonNull(coursePlaceDto.getPlaceCategory())) {
            coursePlace.updatePlaceCategory(coursePlaceDto.getPlaceCategory());
        }
    }
}
