package com.comeon.meetingservice.web.meeting.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.web.common.response.SliceResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.meeting.response.*;
import com.comeon.meetingservice.web.meeting.response.MeetingDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingQueryRepository meetingQueryRepository;
    private final FileManager fileManager;
    private final Environment env;

    public SliceResponse<MeetingListResponse> getList(Long userId,
                                                   Pageable pageable,
                                                   MeetingCondition meetingCondition) {

        Slice<MeetingEntity> resultSlice
                = meetingQueryRepository.findSliceByUserId(userId, pageable, meetingCondition);

        List<MeetingListResponse> meetingListResponses = convertToResponse(resultSlice);

        return SliceResponse.toSliceResponse(resultSlice, meetingListResponses);
    }

    public MeetingDetailResponse getDetail(Long id) {
        MeetingEntity meetingEntity = meetingQueryRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임이 없습니다.", ENTITY_NOT_FOUND));

        return MeetingDetailResponse.toResponse(meetingEntity);
    }

    public String getStoredFileName(Long id) {
        return meetingQueryRepository.findStoredNameById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임이 없습니다.", ENTITY_NOT_FOUND));
    }

    private List<MeetingListResponse> convertToResponse(Slice<MeetingEntity> resultSlice) {
        return resultSlice.getContent().stream()
                .map(meetingEntity -> MeetingListResponse.toResponse(
                        meetingEntity,
                        getFileUrl(meetingEntity.getMeetingFileEntity().getStoredName())))
                .collect(Collectors.toList());
    }

    private String getFileUrl(String fileName) {
        return fileManager.getFileUrl(
                env.getProperty("meeting-file.dir"),
                fileName);
    }

}
