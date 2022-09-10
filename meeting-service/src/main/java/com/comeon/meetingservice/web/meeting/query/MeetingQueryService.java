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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.comeon.meetingservice.common.exception.ErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingQueryRepository meetingQueryRepository;
    private final UserServiceFeignClient userServiceFeignClient;
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
                convertDateResponse(meetingEntity.getMeetingDateEntities()),
                convertPlaceResponse(meetingEntity.getMeetingPlaceEntities()));
    }

    public String getStoredFileName(Long id) {
        return meetingQueryRepository.findStoredNameById(id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임이 없습니다.", ENTITY_NOT_FOUND));
    }

    private List<MeetingListResponse> convertToListResponse(List<MeetingEntity> meetingEntities, Long userId) {
        return meetingEntities.stream()
                .map(meetingEntity -> {
                    // 해당 모임의 확정 날짜들을 구하기
                    List<LocalDate> fixedDates = getMeetingFixedDate(meetingEntity);

                    // 제일 마지막 확정 날짜를 통해 모임의 상태 구하기
                    LocalDate lastFixedDate = getLastFixedDate(fixedDates);

                    Set<MeetingUserEntity> meetingUserEntities = meetingEntity.getMeetingUserEntities();

                    // 요청을 보낸 유저가 해당 모임에서 무슨 역할인지 구하기
                    MeetingRole userMeetingRole = getRequestUserMeetingRole(meetingUserEntities, userId);

                    // 해당 모임에서 HOST인 회원의 닉네임 User Service로부터 가져오기
                    String hostNickname = getHostNickname(meetingUserEntities);

                    return MeetingListResponse.toResponse(
                            meetingEntity,
                            hostNickname,
                            meetingUserEntities.size(),
                            userMeetingRole,
                            getFileUrl(meetingEntity.getMeetingFileEntity().getStoredName()),
                            fixedDates,
                            MeetingStatus.getMeetingStatus(lastFixedDate)
                    );
                })
                .collect(Collectors.toList());
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

    private String getHostNickname(Set<MeetingUserEntity> meetingUserEntities) {
        MeetingUserEntity hostUserEntity = meetingUserEntities.stream()
                .filter(mu -> mu.getMeetingRole() == MeetingRole.HOST)
                .findAny()
                .get();

        UserServiceApiResponse<UserDetailResponse> userResponse =
                userServiceFeignClient.getUser(hostUserEntity.getUserId());

        return userResponse.getData().getNickname();
    }

    private String getFileUrl(String fileName) {
        return fileManager.getFileUrl(
                env.getProperty("meeting-file.dir"),
                fileName);
    }


    private List<MeetingDetailUserResponse> convertUserResponse(Set<MeetingUserEntity> meetingUserEntities) {

        // User Service와 통신하여 회원 정보 리스트 받아오기
        List<Long> userIds = meetingUserEntities.stream()
                .map(MeetingUserEntity::getUserId)
                .collect(Collectors.toList());

        UserServiceApiResponse<UserServiceListResponse<UserListResponse>> userResponses
                = userServiceFeignClient.getUsers(userIds);

        // 받아온 회원 정보를 id: response 형식의 Map으로 만들기
        Map<Long, UserListResponse> userInfoMap = userResponses.getData().getContents().stream()
                .collect(Collectors.toMap(UserListResponse::getUserId, ul -> ul));

        return meetingUserEntities.stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDateTime))
                .map((meetingUserEntity) -> {
                    // MeetingUserEntity와 위에서 만든 userInfoMap과 매핑시키기
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

    private List<MeetingDetailDateResponse> convertDateResponse(Set<MeetingDateEntity> meetingDateEntities) {
        return meetingDateEntities.stream()
                .sorted(Comparator.comparing(MeetingDateEntity::getDate))
                .map(MeetingDetailDateResponse::toResponse)
                .collect(Collectors.toList());
    }

}
