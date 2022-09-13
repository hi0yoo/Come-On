package com.comeon.courseservice.web.course.controller;

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
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.course.request.CourseListRequest;
import com.comeon.courseservice.web.course.request.CourseListRequestValidator;
import com.comeon.courseservice.web.course.request.CourseModifyRequest;
import com.comeon.courseservice.web.course.request.CourseSaveRequest;
import com.comeon.courseservice.web.course.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
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

    private final CourseListRequestValidator courseListRequestValidator;

    @InitBinder("courseListRequest")
    public void init(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(courseListRequestValidator);
    }

    // TODO 로그인 필수
    // 코스 저장 POST /courses
    @ValidationRequired
    @PostMapping
    public ApiResponse<CourseSaveResponse> courseSave(
            @CurrentUserId Long currentUserId,
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
    public ApiResponse<CourseDetailResponse> courseDetails(
            @PathVariable Long courseId,
            @CurrentUserId Long currentUserId) {

        return ApiResponse.createSuccess(
                courseQueryService.getCourseDetails(courseId, currentUserId)
        );
    }

    // 코스 목록 조회 GET /courses
    @ValidationRequired
    @GetMapping
    public ApiResponse<SliceResponse<CourseListResponse>> courseList(
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @Validated @ModelAttribute CourseListRequest courseListRequest,
            BindingResult bindingResult) {

        return ApiResponse.createSuccess(
                courseQueryService.getCourseList(currentUserId, courseListRequest.toCondition(), pageable)
        );
    }

    // TODO [로그인 필수]
    // 내가 등록한 코스 목록 조회 GET /courses/my
    @GetMapping("/my")
    public ApiResponse<SliceResponse<MyPageCourseListResponse>> myCourseList(
            @CurrentUserId Long currentUserId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {

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

        return ApiResponse.createSuccess(
                courseQueryService.getMyLikedCourseList(currentUserId, pageable)
        );
    }

    // TODO [로그인 필수]
    // 코스 수정 POST /courses/{courseId}
    @PostMapping("/{courseId}")
    public ApiResponse<CourseModifyResponse> courseModify(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId,
            @Validated @ModelAttribute CourseModifyRequest request,
            BindingResult bindingResult) {

        // 요청 데이터 -> 코스 dto로 변환
        CourseDto courseDto = request.toServiceDto();
        courseDto.setUserId(currentUserId);

        if (Objects.nonNull(request.getImgFile())) {
            // 이미지 저장 후, 코스 이미지 dto로 변환
            CourseImageDto courseImageDto = generateCourseImageDto(
                    fileManager.upload(request.getImgFile(), dirName)
            );

            // 코스 dto에 이미지 dto 담기
            courseDto.setCourseImageDto(courseImageDto);

            String fileNameToDelete = courseQueryService.getStoredFileName(courseId);
            try {
                courseService.modifyCourse(courseId, courseDto);
            } catch (RuntimeException e) {
                fileNameToDelete = courseImageDto.getStoredName();
                throw e;
            } finally {
                fileManager.delete(fileNameToDelete, dirName);
            }

        } else {
            courseService.modifyCourse(courseId, courseDto);
        }

        return ApiResponse.createSuccess(new CourseModifyResponse());
    }

    // TODO [로그인 필수]
    // 코스 삭제 DELETE /courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ApiResponse<CourseRemoveResponse> courseRemove(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId) {

        String fileNameToDelete = courseQueryService.getStoredFileName(courseId);

        courseService.removeCourse(courseId, currentUserId);

        fileManager.delete(fileNameToDelete, dirName);

        return ApiResponse.createSuccess(new CourseRemoveResponse());
    }


    // TODO [로그인 필수]
    // 코스 좋아요 등록/취소 POST /courses/{courseId}/like
    @PostMapping("/{courseId}/like")
    public ApiResponse<CourseLikeUpdateResponse> courseLikeUpdate(
            @CurrentUserId Long currentUserId,
            @PathVariable Long courseId) {

        return ApiResponse.createSuccess(
                new CourseLikeUpdateResponse(courseLikeService.updateCourseLike(courseId, currentUserId))
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
