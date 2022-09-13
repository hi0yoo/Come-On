package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.CurrentUserId;
import com.comeon.courseservice.domain.courseplace.service.CoursePlaceService;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.aop.ValidationRequired;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.request.PlaceBatchUpdateRequestValidator;
import com.comeon.courseservice.web.courseplace.query.CoursePlaceQueryService;
import com.comeon.courseservice.web.courseplace.request.*;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import com.comeon.courseservice.web.courseplace.response.CoursePlacesBatchUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses/{courseId}/course-places")
public class CoursePlaceController {

    private final CoursePlaceService coursePlaceService;
    private final CoursePlaceQueryService coursePlaceQueryService;

    private final PlaceBatchUpdateRequestValidator placeBatchUpdateRequestValidator;

    @InitBinder("coursePlaceBatchUpdateRequest")
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(placeBatchUpdateRequestValidator);
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
                    .map(CoursePlaceModifyRequest::toServiceDto)
                    .collect(Collectors.toList());
        }

        List<Long> coursePlaceIdsToDelete = new ArrayList<>();
        if (Objects.nonNull(coursePlaceBatchUpdateRequest.getToDelete())) {
            coursePlaceIdsToDelete = coursePlaceBatchUpdateRequest.getToDelete().stream()
                    .map(CoursePlaceDeleteRequest::getCoursePlaceId)
                    .collect(Collectors.toList());
        }

        coursePlaceService.batchUpdateCoursePlace(courseId, currentUserId, dtoToSave, dtoToModify, coursePlaceIdsToDelete);

        return ApiResponse.createSuccess(new CoursePlacesBatchUpdateResponse());
    }

    // 코스 장소 리스트 조회
    @GetMapping
    public ApiResponse<ListResponse<CoursePlaceDetails>> coursePlaceList(@PathVariable Long courseId) {

        return ApiResponse.createSuccess(
                coursePlaceQueryService.getCoursePlaces(courseId)
        );
    }
}
