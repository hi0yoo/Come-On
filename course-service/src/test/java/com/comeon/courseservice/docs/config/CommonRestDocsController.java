package com.comeon.courseservice.docs.config;

import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.web.common.response.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class CommonRestDocsController {

    @GetMapping("/docs/success")
    public ApiResponse<?> commonResponseFields() {
        Map<String, String> responseCodes = Arrays.stream(ApiResponseCode.values())
                .collect(Collectors.toMap(ApiResponseCode::getId, ApiResponseCode::getText));

        return ApiResponse.createSuccess(responseCodes);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> commonErrorResponseFields() {
        return ApiResponse.createServerError(ErrorCode.SERVER_ERROR);
    }

    @GetMapping("/docs/error/codes")
    public ApiResponse<?> errorResponseCodes() {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        return ApiResponse.createSuccess(errorCodes);
    }

    @GetMapping("/docs/slice/response")
    public ApiResponse<SliceResponse> sliceResponseFormat() {
        return ApiResponse.createSuccess(SliceResponse.toSliceResponse(
                new SliceImpl<>(List.of(1, 2, 3), PageRequest.of(0, 10), false)
        ));
    }

    @GetMapping("/docs/list/response")
    public ApiResponse<ListResponse> listResponseFormat() {
        return ApiResponse.createSuccess(
                ListResponse.toListResponse(List.of(1, 2, 3))
        );
    }

    @GetMapping("/course-places/category/codes")
    public ApiResponse<?> coursePlaceCategoryCodes() {
        Map<String, String> categoryCodes = Arrays.stream(CoursePlaceCategory.values())
                .collect(Collectors.toMap(CoursePlaceCategory::name, CoursePlaceCategory::getDescription));
        return ApiResponse.createSuccess(categoryCodes);
    }

    @GetMapping("/courses/status/codes")
    public ApiResponse<?> courseStatusCodes() {
        Map<String, String> courseStatusCodes = Arrays.stream(CourseStatus.values())
                .collect(Collectors.toMap(CourseStatus::name, CourseStatus::getDescription));
        return ApiResponse.createSuccess(courseStatusCodes);
    }
}
