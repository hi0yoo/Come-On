package com.comeon.courseservice.domain.courseplace.service;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CoursePlaceService {

    private final CourseRepository courseRepository;

    public void batchSaveCoursePlace(Long courseId, Long userId, List<CoursePlaceDto> coursePlaceDtoList) {
        Course course = getCourse(courseId);

        // 작성자 확인
        checkWriter(userId, course);

        // cascade.persist 이므로 트랜잭션 종료시, coursePlace 데이터 저장된다.
        coursePlaceDtoList.forEach(coursePlaceDto -> coursePlaceDto.toEntity(course));

        course.completeWriting();
    }

    public void batchModifyCoursePlace(Long courseId, Long userId, List<CoursePlaceDto> coursePlaceDtoList) {
        Course course = getCourse(courseId);

        checkWriter(userId, course);

        // coursePlaceDtoList -> 순서 필드 정리(1부터 시작하게끔, 연속된 수로 정리)
        List<CoursePlaceDto> arrangedCoursePlaceDtoList = arrangeCoursePlaceDtoList(coursePlaceDtoList);

        // 명시되지 않은 coursePlace는 삭제
        course.getCoursePlaces()
                .removeIf(
                        coursePlace -> !arrangedCoursePlaceDtoList.stream()
                                .map(CoursePlaceDto::getCoursePlaceId)
                                .collect(Collectors.toList())
                                .contains(coursePlace.getId())
                ); // 트랜잭션 종료되면 자동 삭제

        // coursePlaceId 값이 있는 dto 필터링(coursePlaceDto.coursePlaceId가 있으면 수정)
        arrangedCoursePlaceDtoList.stream()
                .filter(coursePlaceDto -> Objects.nonNull(coursePlaceDto.getCoursePlaceId()))
                .forEach(
                        coursePlaceDto -> course.getCoursePlaces().stream()
                                .filter(coursePlace -> coursePlace.getId().equals(coursePlaceDto.getCoursePlaceId()))
                                .findFirst()
                                .ifPresent(
                                        coursePlace -> coursePlace.updateCoursePlaceInfo(
                                                coursePlaceDto.getName(),
                                                coursePlaceDto.getDescription(),
                                                coursePlaceDto.getLat(),
                                                coursePlaceDto.getLng(),
                                                coursePlaceDto.getOrder(),
                                                coursePlaceDto.getKakaoPlaceId(),
                                                coursePlaceDto.getPlaceCategory()
                                        )
                                )
                );

        // coursePlaceDto.coursePlaceId가 없으면 생성
        arrangedCoursePlaceDtoList.stream()
                .filter(coursePlaceDto -> Objects.isNull(coursePlaceDto.getCoursePlaceId()))
                .forEach(coursePlaceDto -> coursePlaceDto.toEntity(course));

        // 트랜잭션이 끝나면 삭제, 수정, 생성 모두 반영
    }

    public void batchUpdateCoursePlace(Long courseId, Long userId,
                                       List<CoursePlaceDto> dtosToSave,
                                       List<CoursePlaceDto> dtosToModify,
                                       List<Long> coursePlaceIdsToDelete) {
        /*
        조건 : 중복되는 순서 없음, 중복되는 id 없음, 순서 1부터 연속된 수로 시작
         */

        Course course = getCourse(courseId);

        checkWriter(userId, course);

        List<CoursePlace> coursePlaces = course.getCoursePlaces();
        if (!(coursePlaces.size() == dtosToModify.size() + coursePlaceIdsToDelete.size())) {
            throw new CustomException("수정하려는 데이터가 모두 명시되지 않았습니다.", ErrorCode.VALIDATION_FAIL); // TODO 수정
        }

        // 삭제
        coursePlaceIdsToDelete.forEach(
                coursePlaceId -> coursePlaces.removeIf(coursePlace -> coursePlace.getId().equals(coursePlaceId))
        );

        // 수정
        dtosToModify.forEach(
                coursePlaceDto -> coursePlaces.stream()
                        .filter(coursePlace -> coursePlace.getId().equals(coursePlaceDto.getCoursePlaceId()))
                        .forEach(coursePlace -> modify(coursePlace, coursePlaceDto))
        );

        // 등록
        dtosToSave.forEach(coursePlaceDto -> coursePlaceDto.toEntity(course));
    }


    /* === private method === */
    private List<CoursePlaceDto> arrangeCoursePlaceDtoList(List<CoursePlaceDto> coursePlaceDtoList) {
        AtomicInteger atomicInteger = new AtomicInteger();
        return coursePlaceDtoList.stream()
                .sorted(Comparator.comparingInt(CoursePlaceDto::getOrder))
                .peek(coursePlaceDto -> coursePlaceDto.setOrder(atomicInteger.incrementAndGet()))
                .collect(Collectors.toList());
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findByIdFetchCoursePlaces(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId)
                );
    }

    private void checkWriter(Long userId, Course course) {
        if (!Objects.equals(course.getUserId(), userId)) {
            throw new CustomException("해당 코스에 장소를 등록 할 권한이 없습니다. 요청한 유저 식별값 : " + userId, ErrorCode.NO_AUTHORITIES);
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
