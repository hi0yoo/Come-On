package com.comeon.courseservice.web.course.controller;

import com.comeon.courseservice.config.argresolver.CurrentUserId;
import com.comeon.courseservice.domain.course.service.CourseService;
import com.comeon.courseservice.domain.course.service.dto.CourseDto;
import com.comeon.courseservice.domain.course.service.dto.CourseImageDto;
import com.comeon.courseservice.web.common.aop.ValidationRequired;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.course.request.CourseSaveRequest;
import com.comeon.courseservice.web.course.response.CourseSaveResponse;
import com.comeon.courseservice.web.course.response.CourseWritingDoneResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/courses")
public class CourseController {

    @Value("${profile.dirName}")
    private String dirName;

    private final FileManager fileManager;
    private final CourseService courseService;

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

    // 코스 작성 완료 처리 PATCH /courses/{courseId}/done
    @PatchMapping("/{courseId}/done")
    public ApiResponse<?> courseWritingDone(@CurrentUserId Long currentUserId,
                                            @PathVariable Long courseId) {
        courseService.completeWritingCourse(courseId, currentUserId);

        return ApiResponse.createSuccess(new CourseWritingDoneResponse());
    }

    // 코스 단건 조회 GET /courses/{courseId}

    // 코스 목록 조회 GET /courses

    // 코스 수정 PATCH /courses/{courseId}

    // 코스 삭제 DELETE /courses/{courseId}

    // 코스 장소 리스트 조회 GET /courses/{courseId}/course-places

    // 코스 좋아요


    /* ### private method ### */
    private CourseImageDto generateCourseImageDto(UploadedFileInfo uploadedFileInfo) {
        return CourseImageDto.builder()
                .originalName(uploadedFileInfo.getOriginalFileName())
                .storedName(uploadedFileInfo.getStoredFileName())
                .build();
    }
}
