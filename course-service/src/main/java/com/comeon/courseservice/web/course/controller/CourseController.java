package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.config.argresolver.CurrentUserId;
import com.comeon.courseservice.domain.course.service.CourseService;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.domain.courselike.service.CourseLikeService;
import com.comeon.courseservice.web.common.aop.ValidationRequired;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.common.response.SliceResponse;
import com.comeon.courseservice.web.course.query.CourseCondition;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.course.request.CourseSaveRequest;
import com.comeon.courseservice.web.course.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    @Value("${s3.folder-name.course}")
    private String dirName;

    private final FileManager fileManager;
    private final CourseService courseService;
    private final CourseLikeService courseLikeService;

    private final CourseQueryService courseQueryService;

    // TODO 로그인 필수
    // 코스 저장 POST /courses
    @ValidationRequired
    @PostMapping
    public ApiResponse<CourseSaveResponse> courseSave(@CurrentUserId Long currentUserId,
                                                      @Validated @ModelAttribute CourseSaveRequest request,
                                                      BindingResult bindingResult) {
        // 이미지 저장 후, 코스 이미지 dto로 변환
        CourseImageDto courseImageDto = generateCourseImageDto(
                fileManager.upload(request.getImgFile(), dirName)
        );

        // 요청 데이터 -> 코스 dto로 변환
        CourseDto courseDto = request.toServiceDto();
        courseDto.setUserId(currentUserId);
        courseDto.setCourseImageDto(courseImageDto);

        Long courseId = null;
        try {
            courseId = courseService.saveCourse(courseDto);
        } catch (RuntimeException e) {
            fileManager.delete(courseImageDto.getStoredName(), dirName);
            throw e;
        }

        return ApiResponse.createSuccess(new CourseSaveResponse(courseId));
    }

    // 코스 단건 조회 GET /courses/{courseId}
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailResponse> courseDetails(@PathVariable Long courseId,
                                                           @CurrentUserId Long currentUserId) {
        CourseDetailResponse courseDetails = courseQueryService.getCourseDetails(courseId, currentUserId);

        return ApiResponse.createSuccess(courseDetails);
    }

    // 코스 목록 조회 GET /courses
    @GetMapping
    public ApiResponse<SliceResponse<CourseListResponse>> courseList(
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            CourseCondition condition) {

        return ApiResponse.createSuccess(
                courseQueryService.getCourseList(currentUserId, condition, pageable)
        );
    }

    // TODO [로그인 필수]
    // 내가 등록한 코스 목록 조회 GET /courses/my
    @GetMapping("/my")
    public ApiResponse<SliceResponse<MyPageCourseListResponse>> myCourseList(
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

        if (Objects.isNull(currentUserId)) {
            throw new CustomException("로그인이 필요한 기능입니다.", ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        return ApiResponse.createSuccess(
                courseQueryService.getMyRegisteredCourseList(currentUserId, pageable)
        );
    }

    // TODO [로그인 필수]
    // GET /courses/like - 코스 좋아요 목록
    @GetMapping("/like")
    public ApiResponse<SliceResponse<MyPageCourseListResponse>> courseLikeList(
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

        if (Objects.isNull(currentUserId)) {
            throw new CustomException("로그인이 필요한 기능입니다.", ErrorCode.INVALID_AUTHORIZATION_HEADER);
        }

        return ApiResponse.createSuccess(
                courseQueryService.getMyLikedCourseList(currentUserId, pageable)
        );
    }

    // TODO [로그인 필수]
    // 코스 수정 PATCH /courses/{courseId}

    // TODO [로그인 필수]
    // 코스 삭제 DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ApiResponse<CourseRemoveResponse> courseRemove(@CurrentUserId Long currentUserId,
                                                          @PathVariable Long courseId) {

        courseService.removeCourse(courseId, currentUserId);

        return ApiResponse.createSuccess(new CourseRemoveResponse());
    }

    // TODO [로그인 필수]
    // 코스 좋아요 변경 POST /courses/{courseId}/like
    @PostMapping("/{courseId}/like")
    public ApiResponse<CourseLikeModifyResponse> courseLikeModify(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId) {

        return ApiResponse.createSuccess(
                new CourseLikeModifyResponse(courseLikeService.modifyCourseLike(courseId, currentUserId))
        );
    }


    /* ### private method ### */
    private CourseImageDto generateCourseImageDto(UploadedFileInfo uploadedFileInfo) {
        return CourseImageDto.builder()
                .originalName(uploadedFileInfo.getOriginalFileName())
                .storedName(uploadedFileInfo.getStoredFileName())
                .build();
    }
}
