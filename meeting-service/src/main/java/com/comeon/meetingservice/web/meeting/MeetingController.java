package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.aop.ValidationRequired;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.interceptor.MeetingAuth;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.response.SliceResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.query.MeetingQueryService;
import com.comeon.meetingservice.web.meeting.query.MeetingCondition;
import com.comeon.meetingservice.web.meeting.request.MeetingModifyRequest;
import com.comeon.meetingservice.web.meeting.request.MeetingAddRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final Environment env;
    private final MeetingService meetingService;
    private final MeetingQueryService meetingQueryService;
    private final FileManager fileManager;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ValidationRequired
    public ApiResponse<Long> meetingAdd(@Validated @ModelAttribute MeetingAddRequest meetingAddRequest,
                                        BindingResult bindingResult,
                                        @UserId Long userId) {

        UploadFileDto uploadFileDto = uploadImage(meetingAddRequest.getImage());

        MeetingAddDto meetingAddDto = meetingAddRequest.toDto();
        meetingAddDto.setUserId(userId);
        meetingAddDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
        meetingAddDto.setStoredFileName(uploadFileDto.getStoredFileName());

        Long savedId;
        try {
            savedId = meetingService.add(meetingAddDto);
        } catch (RuntimeException e) {
            deleteImage(uploadFileDto.getStoredFileName());
            throw e;
        }

        return ApiResponse.createSuccess(savedId);
    }

    @PostMapping("/{meetingId}")
    @ValidationRequired
    @MeetingAuth(meetingRoles = MeetingRole.HOST)
    public ApiResponse meetingModify(@PathVariable("meetingId") Long meetingId,
                                     @Validated @ModelAttribute MeetingModifyRequest meetingModifyRequest,
                                     BindingResult bindingResult) {

        MeetingModifyDto meetingModifyDto = meetingModifyRequest.toDto();
        meetingModifyDto.setId(meetingId);

        // 파일을 수정한다면
        if (Objects.nonNull(meetingModifyRequest.getImage())) {
            // 수정될 파일 우선 저장
            UploadFileDto uploadFileDto = uploadImage(meetingModifyRequest.getImage());
            meetingModifyDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
            meetingModifyDto.setStoredFileName(uploadFileDto.getStoredFileName());

            // 수정에 성공하면 이전에 저장됐던 파일을 삭제하기 위해 저장 파일명 조회하기 (커맨드와 쿼리 분리)
            String fileNameToDelete = meetingQueryService.getStoredFileName(meetingId);
            try {
                // DB에 반영 후 이전에 저장됐던 파일의 이름을 deleteFile 변수에 넣어놓음
                meetingService.modify(meetingModifyDto);

                // Service 로직 실행 도중에 예외 발생 시에는 파일 변경이 이뤄지면 안됨,
                // 즉 fileNameToDelete 변수의 값을 저장한 변경 파일의 이름으로 바꿈
            } catch (RuntimeException e) {
                fileNameToDelete = uploadFileDto.getStoredFileName();
                throw e;
            } finally {
                // 최종적으로 예외가 발생했을 경우에는 변경 파일이 지워지고, 예외가 발생하지 않았다면 기존의 파일이 지워지는 구조
                deleteImage(fileNameToDelete);
            }
        } else {
            // 파일이 변경되지 않는다면 그냥 변경된 값만 단순 변경, 파일은 건드릴 필요 없음
            meetingService.modify(meetingModifyDto);
        }

        return ApiResponse.createSuccess();
    }

    @DeleteMapping("/{meetingId}")
    @MeetingAuth(meetingRoles = {MeetingRole.HOST, MeetingRole.EDITOR, MeetingRole.PARTICIPANT})
    public ApiResponse meetingRemove(@PathVariable("meetingId") Long meetingId,
                                     @UserId Long userId) {

        String storedFileName = meetingQueryService.getStoredFileName(meetingId);

        meetingService.remove(
                MeetingRemoveDto.builder()
                        .id(meetingId)
                        .userId(userId)
                        .build()
        );

        deleteImage(storedFileName);
        return ApiResponse.createSuccess();
    }

    @GetMapping
    public ApiResponse<SliceResponse> meetingList(@UserId Long userId,
                                                  @PageableDefault(size = 5, page = 0) Pageable pageable,
                                                  MeetingCondition meetingCondition) {
        return ApiResponse.createSuccess(
                meetingQueryService.getList(userId, pageable, meetingCondition));
    }

    @GetMapping("/{meetingId}")
    @MeetingAuth(meetingRoles = {MeetingRole.HOST, MeetingRole.EDITOR, MeetingRole.PARTICIPANT})
    public ApiResponse<MeetingDetailResponse> meetingDetail(@PathVariable("meetingId") Long meetingId,
                                                            @UserId Long userId) {
        return ApiResponse.createSuccess(
                meetingQueryService.getDetail(meetingId, userId));
    }

    private UploadFileDto uploadImage(MultipartFile image) {
        return fileManager.upload(
                image,
                env.getProperty("meeting-file.dir"));
    }

    private void deleteImage(String storedFileName) {
        fileManager.delete(
                storedFileName,
                env.getProperty("meeting-file.dir"));
    }
}
