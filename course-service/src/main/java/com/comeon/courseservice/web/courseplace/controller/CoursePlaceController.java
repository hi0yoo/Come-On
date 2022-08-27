package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.config.argresolver.CurrentUserId;
import com.comeon.courseservice.domain.courseplace.service.CoursePlaceService;
import com.comeon.courseservice.domain.courseplace.service.dto.CoursePlaceDto;
import com.comeon.courseservice.web.common.aop.ValidationRequired;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.courseplace.request.CoursePlaceSaveRequest;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceSaveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/course-places")
public class CoursePlaceController {

    private final CoursePlaceService coursePlaceService;

    // 코스 장소 등록
    @ValidationRequired
    @PostMapping
    public ApiResponse<CoursePlaceSaveResponse> coursePlaceSave(@CurrentUserId Long currentUserId,
                                                                @Validated @RequestBody CoursePlaceSaveRequest request,
                                                                BindingResult bindingResult) {
        Long courseId = request.getCourseId();
        CoursePlaceDto coursePlaceDto = request.toServiceDto();

        Long coursePlaceId = coursePlaceService.saveCoursePlace(courseId, currentUserId, coursePlaceDto);

        return ApiResponse.createSuccess(new CoursePlaceSaveResponse(coursePlaceId));
    }
}
