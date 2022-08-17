package com.comeon.meetingservice.web.meeting;

import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingSaveDto;
import com.comeon.meetingservice.domain.meeting.service.MeetingService;
import com.comeon.meetingservice.web.common.argumentresolver.UserId;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.response.SliceResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.common.util.fileutils.UploadFileDto;
import com.comeon.meetingservice.web.meeting.query.MeetingQueryRepository;
import com.comeon.meetingservice.web.meeting.query.dto.MeetingCondition;
import com.comeon.meetingservice.web.meeting.query.dto.MeetingQueryListDto;
import com.comeon.meetingservice.web.meeting.request.MeetingModifyRequest;
import com.comeon.meetingservice.web.meeting.request.MeetingSaveRequest;
import com.comeon.meetingservice.web.meeting.response.MeetingListResponse;
import com.comeon.meetingservice.web.meeting.response.MeetingModifyResponse;
import com.comeon.meetingservice.web.meeting.response.MeetingRemoveResponse;
import com.comeon.meetingservice.web.meeting.response.MeetingSaveResponse;
import com.comeon.meetingservice.web.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final Environment env;
    private final MeetingService meetingService;
    private final MeetingQueryRepository meetingQueryRepository;
    private final FileManager fileManager;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MeetingSaveResponse> meetingAdd(@Validated @ModelAttribute MeetingSaveRequest meetingSaveRequest,
                                                       BindingResult bindingResult,
                                                       @UserId Long userId) {
        ValidationUtils.validate(bindingResult);

        UploadFileDto uploadFileDto = uploadImage(meetingSaveRequest.getImage());

        MeetingSaveDto meetingSaveDto = meetingSaveRequest.toDto();
        meetingSaveDto.setUserId(userId);
        meetingSaveDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
        meetingSaveDto.setStoredFileName(uploadFileDto.getStoredFileName());

        MeetingSaveDto resultDto;
        try {
            resultDto = meetingService.add(meetingSaveDto);
        } catch (RuntimeException e) {
            deleteImage(uploadFileDto.getStoredFileName());
            throw e;
        }

        return ApiResponse.createSuccess(MeetingSaveResponse.toResponse(resultDto));
    }

    @PatchMapping("/{meetingId}")
    public ApiResponse<MeetingModifyResponse> meetingModify(@PathVariable("meetingId") Long meetingId,
                                                            @Validated @ModelAttribute MeetingModifyRequest meetingModifyRequest,
                                                            BindingResult bindingResult) {
        ValidationUtils.validate(bindingResult);

        MeetingModifyDto meetingModifyDto = meetingModifyRequest.toDto();
        meetingModifyDto.setId(meetingId);

        MeetingModifyDto resultDto = null;

        // 파일을 수정한다면
        if (Objects.nonNull(meetingModifyRequest.getImage())) {
            // 수정될 파일 우선 저장
            UploadFileDto uploadFileDto = uploadImage(meetingModifyRequest.getImage());
            meetingModifyDto.setOriginalFileName(uploadFileDto.getOriginalFileName());
            meetingModifyDto.setStoredFileName(uploadFileDto.getStoredFileName());
            String deleteFile = null;
            try {
                // DB에 반영 후 이전에 저장됐던 파일의 이름을 deleteFile 변수에 넣어놓음
                resultDto = meetingService.modify(meetingModifyDto);
                deleteFile = resultDto.getBeforeStoredFileName();

                // Service 로직 실행 도중에 예외 발생 시에는 파일 변경이 이뤄지면 안됨, 즉 저장했던 변경 파일을 deleteFile 변수에 넣어놓음
            } catch (RuntimeException e) {
                deleteFile = uploadFileDto.getStoredFileName();
                throw e;
            } finally {
                // 최종적으로 예외가 발생했을 경우에는 변경 파일이 지워지고, 예외가 발생하지 않았다면 기존의 파일이 지워지는 구조
                deleteImage(deleteFile);
            }
        } else {
            // 파일이 변경되지 않는다면 그냥 변경된 값만 단순 변경, 파일은 건드릴 필요 없음
            resultDto = meetingService.modify(meetingModifyDto);
        }

        return ApiResponse.createSuccess(MeetingModifyResponse.toResponse(resultDto));
    }

    @DeleteMapping("/{meetingId}")
    public ApiResponse<MeetingRemoveResponse> meetingRemove(@PathVariable("meetingId") Long meetingId,
                                                            @UserId Long userId) {
        MeetingRemoveDto resultDto = meetingService.remove(
                MeetingRemoveDto.builder()
                        .id(meetingId)
                        .userId(userId)
                        .build()
        );

        return ApiResponse.createSuccess(MeetingRemoveResponse.toResponse(resultDto));
    }

    @GetMapping
    public ApiResponse<SliceResponse> meetingList(@UserId Long userId,
                                                  @PageableDefault(size = 5, page = 0) Pageable pageable,
                                                  MeetingCondition meetingCondition) {
        Slice<MeetingQueryListDto> resultSlice =
                meetingQueryRepository.findSliceByUserId(userId, pageable, meetingCondition);

        List<MeetingListResponse> meetingListResponses = resultSlice.getContent().stream()
                .map(m -> MeetingListResponse.toResponse(m, getFileUrl(m.getStoredName())))
                .collect(Collectors.toList());

        return ApiResponse.createSuccess(SliceResponse.toSliceResponse(resultSlice, meetingListResponses));
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

    private String getFileUrl(String fileName) {
        return fileManager.getFileUrl(
                env.getProperty("meeting-file.dir"),
                fileName);
    }
}
