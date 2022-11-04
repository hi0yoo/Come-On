package com.comeon.meetingservice.web.meeting.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.feign.userservice.*;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserListResponse;
import com.comeon.meetingservice.web.common.response.SliceResponse;
import com.comeon.meetingservice.web.common.util.fileutils.FileManager;
import com.comeon.meetingservice.web.meeting.response.*;
import com.comeon.meetingservice.web.meeting.response.MeetingDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingQueryRepository meetingQueryRepository;
    private final UserFeignService userFeignService;
    private final FileManager fileManager;
    private final Environment env;

    public SliceResponse<MeetingListResponse> getList(Long userId,
                                                   Pageable pageable,
                                                   MeetingCondition meetingCondition) {

        Slice<MeetingEntity> resultSlice
                = meetingQueryRepository.findSliceByUserId(userId, pageable, meetingCondition);

        List<MeetingListResponse> meetingListResponses = convertToListResponse(resultSlice.getContent(), userId);

        return SliceResponse.toSliceResponse(resultSlice, meetingListResponses);
    }

    public MeetingDetailResponse getDetail(Long id, Long userId) {
        MeetingEntity meetingEntity = meetingQueryRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임이 없습니다.", ENTITY_NOT_FOUND));

        MeetingUserEntity currentUser = meetingEntity.getMeetingUserEntities().stream()
                .filter(mu -> mu.getUserId().equals(userId))
                .findAny()
                .orElseThrow(() -> new CustomException("해당 회원은 가입되어있지 않습니다.", MEETING_USER_NOT_INCLUDE));

        return MeetingDetailResponse.toResponse(
                meetingEntity,
                currentUser,
                convertUserResponse(meetingEntity.getMeetingUserEntities()),
                convertDateResponse(meetingEntity.getMeetingDateEntities(), userId),
                convertPlaceResponse(meetingEntity.getMeetingPlaceEntities()));
    }

    public String getStoredFileName(Long id) {
        return meetingQueryRepository.findStoredNameById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임이 없습니다.", ENTITY_NOT_FOUND));
    }

    private List<MeetingListResponse> convertToListResponse(List<MeetingEntity> meetingEntities, Long userId) {

        // 조회된 모임 리스트의 호스트 Id만 리스트로 모으기
        Set<Long> hostUserIds = new HashSet<>();
        meetingEntities.stream().forEach(meetingEntity -> {
            meetingEntity.getMeetingUserEntities().stream()
                    .filter(meetingUserEntity -> meetingUserEntity.getMeetingRole() == MeetingRole.HOST)
                    .forEach((meetingUserEntity) -> hostUserIds.add(meetingUserEntity.getUserId()));
        });

        // User Service로 부터 Host회원의 정보 리스트로 가져오기
        Map<Long, UserListResponse> hostUserInfoMap = userFeignService.getUserInfoMap(hostUserIds);

        return meetingEntities.stream()
                .map(meetingEntity -> {
                    // 해당 모임의 확정 날짜들을 구하기
                    List<LocalDate> fixedDates = getMeetingFixedDate(meetingEntity);

                    // 제일 마지막 확정 날짜를 통해 모임의 상태 구하기
                    LocalDate lastFixedDate = getLastFixedDate(fixedDates);

                    // 요청을 보낸 유저가 해당 모임에서 무슨 역할인지 구하기
                    Set<MeetingUserEntity> meetingUserEntities = meetingEntity.getMeetingUserEntities();
                    MeetingRole userMeetingRole = getRequestUserMeetingRole(meetingUserEntities, userId);

                    // 해당 모임에서 HOST인 회원의 정보를 추출하기 (위에서 User Service로부터 정보를 얻어옴)
                    UserListResponse hostUserInfo = hostUserInfoMap.get(getHostUserId(meetingUserEntities));

                    return MeetingListResponse.toResponse(
                            meetingEntity,
                            hostUserInfo.getNickname(),
                            meetingUserEntities.size(),
                            userMeetingRole,
                            getFileUrl(meetingEntity.getMeetingFileEntity().getStoredName()),
                            fixedDates,
                            MeetingStatus.getMeetingStatus(lastFixedDate)
                    );
                })
                .collect(Collectors.toList());
    }

    private Long getHostUserId(Set<MeetingUserEntity> meetingUserEntities) {
        MeetingUserEntity hostUserEntity = meetingUserEntities.stream()
                .filter(mu -> mu.getMeetingRole() == MeetingRole.HOST)
                .findAny()
                .get();
        return hostUserEntity.getUserId();
    }

    private List<LocalDate> getMeetingFixedDate(MeetingEntity meetingEntity) {
        return meetingEntity.getMeetingDateEntities().stream()
                .filter(md -> md.getDateStatus().equals(DateStatus.FIXED))
                .sorted(Comparator.comparing(MeetingDateEntity::getDate))
                .map(MeetingDateEntity::getDate)
                .collect(Collectors.toList());
    }

    private LocalDate getLastFixedDate(List<LocalDate> fixedDates) {
        if (!fixedDates.isEmpty()) {
            return fixedDates.get(fixedDates.size() - 1);
        }
        return null;
    }

    private MeetingRole getRequestUserMeetingRole(Set<MeetingUserEntity> meetingUserEntities, Long userId) {
        return meetingUserEntities.stream()
                .filter((mu) -> mu.getUserId().equals(userId))
                .findAny()
                .get() // 애초에 UserId로 조회가 된 엔티티가 넘어오기에 null일 가능성이 아예 없음
                .getMeetingRole();
    }

    private String getFileUrl(String fileName) {
        return fileManager.getFileUrl(
                env.getProperty("meeting-file.dir"),
                fileName);
    }

    private List<MeetingDetailUserResponse> convertUserResponse(Set<MeetingUserEntity> meetingUserEntities) {
        // User Service와 통신하여 회원 정보 Map을 받아오기
        Set<Long> userIds = meetingUserEntities.stream()
                .map(MeetingUserEntity::getUserId)
                .collect(Collectors.toSet());

        Map<Long, UserListResponse> userInfoMap = userFeignService.getUserInfoMap(userIds);

        return meetingUserEntities.stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDateTime))
                .map((meetingUserEntity) -> {
                    UserListResponse userInfo = userInfoMap.get(meetingUserEntity.getUserId());
                    return MeetingDetailUserResponse.toResponse(
                            meetingUserEntity,
                            userInfo.getNickname(),
                            userInfo.getProfileImageUrl());
                })
                .collect(Collectors.toList());
    }

    private List<MeetingDetailPlaceResponse> convertPlaceResponse(Set<MeetingPlaceEntity> meetingPlaceEntities) {
        return meetingPlaceEntities.stream()
                .sorted(Comparator.comparing(MeetingPlaceEntity::getOrder))
                .map(MeetingDetailPlaceResponse::toResponse)
                .collect(Collectors.toList());
    }

    private List<MeetingDetailDateResponse> convertDateResponse(Set<MeetingDateEntity> meetingDateEntities, Long userId) {
        return meetingDateEntities.stream()
                .sorted(Comparator.comparing(MeetingDateEntity::getDate))
                .map(md -> MeetingDetailDateResponse.toResponse(md, userId))
                .collect(Collectors.toList());
    }

}
