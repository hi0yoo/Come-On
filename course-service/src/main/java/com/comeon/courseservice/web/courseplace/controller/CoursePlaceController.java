package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.CurrentUserId;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.service.CoursePlaceService;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.aop.ValidationRequired;
import com.comeon.courseservice.web.common.exception.ValidateException;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.courseplace.request.PlaceBatchUpdateRequestValidator;
import com.comeon.courseservice.web.courseplace.query.CoursePlaceQueryService;
import com.comeon.courseservice.web.courseplace.request.*;
import com.comeon.courseservice.web.courseplace.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses/{courseId}/course-places")
public class CoursePlaceController {

    private final CoursePlaceService coursePlaceService;
    private final CoursePlaceQueryService coursePlaceQueryService;

    private final CourseQueryService courseQueryService;

    private final PlaceBatchUpdateRequestValidator placeBatchUpdateRequestValidator;

    @InitBinder("coursePlaceBatchUpdateRequest")
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(placeBatchUpdateRequestValidator);
    }

    @ValidationRequired
    @PostMapping
    public ApiResponse<CoursePlaceAddResponse> coursePlaceAdd(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId,
            @Validated @RequestBody CoursePlaceAddRequest request,
            BindingResult bindingResult) {
        CoursePlaceDto coursePlaceDto = request.toServiceDto();
        Long coursePlaceId = coursePlaceService.coursePlaceAdd(courseId, currentUserId, coursePlaceDto);

        return ApiResponse.createSuccess(
                coursePlaceQueryService.getCoursePlaceAddResponse(courseId, coursePlaceId)
        );
    }

    @ValidationRequired
    @PatchMapping("/{coursePlaceId}")
    public ApiResponse<CoursePlaceModifyResponse> coursePlaceModify(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId,
            @PathVariable Long coursePlaceId,
            @Validated @RequestBody CoursePlaceModifyRequest request,
            BindingResult bindingResult) {
        CoursePlaceDto coursePlaceDto = request.toServiceDto();
        coursePlaceService.coursePlaceModify(courseId, currentUserId, coursePlaceId, coursePlaceDto);

        return ApiResponse.createSuccess(coursePlaceQueryService.getCoursePlaceModifyResponse(courseId));
    }

    @DeleteMapping("/{coursePlaceId}")
    public ApiResponse<CoursePlaceDeleteResponse> coursePlaceDelete(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId,
            @PathVariable Long coursePlaceId) {
        coursePlaceService.coursePlaceRemove(courseId, currentUserId, coursePlaceId);

        return ApiResponse.createSuccess(coursePlaceQueryService.getCoursePlaceDeleteResponse(courseId));
    }

    // 코스 장소 리스트 등록/수정/삭제
    @ValidationRequired
    @PostMapping("/batch")
    public ApiResponse<CoursePlacesBatchUpdateResponse> coursePlaceUpdateBatch(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId,
            @Validated @RequestBody CoursePlaceBatchUpdateRequest coursePlaceBatchUpdateRequest,
            BindingResult bindingResult) {

        List<CoursePlaceDto> dtoToSave = new ArrayList<>();
        if (Objects.nonNull(coursePlaceBatchUpdateRequest.getToSave())) {
            dtoToSave = coursePlaceBatchUpdateRequest.getToSave().stream()
                    .map(CoursePlaceSaveRequest::toServiceDto)
                    .collect(Collectors.toList());
        }

        List<CoursePlaceDto> dtoToModify = new ArrayList<>();
        if (Objects.nonNull(coursePlaceBatchUpdateRequest.getToModify())) {
            dtoToModify = coursePlaceBatchUpdateRequest.getToModify().stream()
                    .map(CoursePlaceModifyRequestForBatch::toServiceDto)
                    .collect(Collectors.toList());
        }

        List<Long> coursePlaceIdsToDelete = new ArrayList<>();
        if (Objects.nonNull(coursePlaceBatchUpdateRequest.getToDelete())) {
            coursePlaceIdsToDelete = coursePlaceBatchUpdateRequest.getToDelete().stream()
                    .map(CoursePlaceDeleteRequest::getId)
                    .collect(Collectors.toList());
        }

        validateCoursePlaces(courseId, dtoToSave, dtoToModify, coursePlaceIdsToDelete);

        coursePlaceService.batchUpdateCoursePlace(courseId, currentUserId, dtoToSave, dtoToModify, coursePlaceIdsToDelete);

        return ApiResponse.createSuccess(
                new CoursePlacesBatchUpdateResponse(
                        courseId,
                        courseQueryService.getCourseStatus(courseId)
                )
        );
    }

    // 코스 장소 리스트 조회
    @GetMapping
    public ApiResponse<ListResponse<CoursePlaceDetails>> coursePlaceList(@PathVariable Long courseId) {

        return ApiResponse.createSuccess(
                coursePlaceQueryService.getCoursePlaceListResponse(courseId)
        );
    }


    /* ==== private method ==== */
    private void validateCoursePlaces(Long courseId,
                                      List<CoursePlaceDto> dtoToSave,
                                      List<CoursePlaceDto> dtoToModify,
                                      List<Long> coursePlaceIdsToDelete) {
        List<Long> originalCoursePlaceIds = coursePlaceQueryService.getCoursePlaceIds(courseId);

        // 수정 및 삭제 데이터에 명시된 placeId 들이 originalCoursePlaceIds 에 하나라도 포함되지 않으면 예외
        List<Long> toUpdateCoursePlaceIds = Stream.concat(
                        dtoToModify.stream().map(CoursePlaceDto::getCoursePlaceId),
                        coursePlaceIdsToDelete.stream()
                )
                .collect(Collectors.toList());

        if (!originalCoursePlaceIds.containsAll(toUpdateCoursePlaceIds)) {
            LinkedMultiValueMap<String, String> errorResult = new LinkedMultiValueMap<>();
            errorResult.add("Global", "수정 요청 데이터 혹은, 삭제 요청 데이터에 해당 코스의 장소가 아닌 장소 식별값이 포함되어 있습니다. 확인해주세요.");
            throw new ValidateException("요청 데이터에 수정하려는 코스에 속하지 않는 장소가 있습니다.", errorResult);
        }

//        // toSave 없이 toDelete 와 originalCoursePlaceIds 의 크기가 같으면, 코스의 장소 리스트가 0이 되므로 예외
//        if (dtoToSave.size() == 0 && coursePlaceIdsToDelete.size() >= originalCoursePlaceIds.size()) {
//            LinkedMultiValueMap<String, String> errorResult = new LinkedMultiValueMap<>();
//            errorResult.add("Global", "코스의 장소 개수는 0개가 될 수 없습니다. 코스에 최소 하나 이상의 장소가 등록되어 있도록 해주세요.");
//            throw new ValidateException("코스에 장소가 최소 하나 이상 등록되어야 합니다.", errorResult);
//        }
    }
}
