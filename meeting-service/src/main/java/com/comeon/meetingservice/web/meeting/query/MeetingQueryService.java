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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
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
    private final UserServiceFeignClient userServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
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

        // 조회된 모임 리스트의 호스트 Id만 리스트로 모으기
        List<Long> hostUserIds = new ArrayList<>();
        meetingEntities.stream().forEach(meetingEntity -> {
            meetingEntity.getMeetingUserEntities().stream()
                    .filter(meetingUserEntity -> meetingUserEntity.getMeetingRole() == MeetingRole.HOST)
                    .forEach((meetingUserEntity) -> hostUserIds.add(meetingUserEntity.getUserId()));
        });

        // User Service로 부터 Host회원의 정보 리스트로 가져오기
        Map<Long, UserListResponse> hostUserInfoMap = getUserInfoMap(hostUserIds);

        return meetingEntities.stream()
                .map(meetingEntity -> {
                    // 해당 모임의 확정 날짜들을 구하기
                    List<LocalDate> fixedDates = getMeetingFixedDate(meetingEntity);

                    // 제일 마지막 확정 날짜를 통해 모임의 상태 구하기
                    LocalDate lastFixedDate = getLastFixedDate(fixedDates);

                    Set<MeetingUserEntity> meetingUserEntities = meetingEntity.getMeetingUserEntities();

                    // 요청을 보낸 유저가 해당 모임에서 무슨 역할인지 구하기
                    MeetingRole userMeetingRole = getRequestUserMeetingRole(meetingUserEntities, userId);

                    // 해당 모임에서 HOST인 회원의 정보를 추출하기 (위에서 User Service로부터 정보를 얻어옴)
                    String hostNickname = getHostUserNickname(hostUserInfoMap, meetingUserEntities);

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

    private String getHostUserNickname(Map<Long, UserListResponse> hostUserInfoMap, Set<MeetingUserEntity> meetingUserEntities) {
        MeetingUserEntity hostUserEntity = meetingUserEntities.stream()
                .filter(mu -> mu.getMeetingRole() == MeetingRole.HOST)
                .findAny()
                .get();

        if (Objects.nonNull(hostUserInfoMap.get(hostUserEntity.getUserId()))) {
            return hostUserInfoMap.get(hostUserEntity.getUserId()).getNickname();
        } else {
            return null;
        }
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

        // User Service와 통신하여 회원 정보 리스트 받아오기
        List<Long> userIds = meetingUserEntities.stream()
                .map(MeetingUserEntity::getUserId)
                .collect(Collectors.toList());

        Map<Long, UserListResponse> userInfoMap = getUserInfoMap(userIds);

        return meetingUserEntities.stream()
                .sorted(Comparator.comparing(BaseEntity::getCreatedDateTime))
                .map((meetingUserEntity) -> {
                    // MeetingUserEntity와 위에서 만든 userInfoMap과 매핑시키기
                    UserListResponse userInfo = userInfoMap.get(meetingUserEntity.getUserId());

                    // UserService가 장애가 발생하거나, 조회된 회원이 없거나, 탈퇴한 회원일 경우에는 단순 응답하지 않음
                    String nickname = null;
                    String profileImageUrl = null;

                    if (Objects.nonNull(userInfo)) {
                        nickname = userInfo.getNickname();
                        profileImageUrl = userInfo.getProfileImageUrl();
                    }

                    return MeetingDetailUserResponse.toResponse(
                            meetingUserEntity,
                            nickname,
                            profileImageUrl);
                })
                .collect(Collectors.toList());
    }

    // User Service와 통신하여 id: userInfo 형식의 Map으로 정보를 변환해주는 메서드
    private Map<Long, UserListResponse> getUserInfoMap(List<Long> userIds) {
        CircuitBreaker userListCb = circuitBreakerFactory.create("userList");
        UserServiceApiResponse<UserServiceListResponse<UserListResponse>> userResponses
                = userListCb.run(() -> userServiceFeignClient.getUsers(userIds),
                throwable -> {
                    log.error("[User Service Error]", throwable);
                    return null;
                });

        Map<Long, UserListResponse> userInfoMap = new HashMap<>();
        if (Objects.nonNull(userResponses)) {
            // 받아온 회원 정보 중 ACTIVATE인 회원만 id: response 형식의 Map으로 만들기
            userInfoMap = userResponses.getData().getContents().stream()
                    .filter(response -> response.getStatus() == UserStatus.ACTIVATE)
                    .collect(Collectors.toMap(UserListResponse::getUserId, ul -> ul));
        }

        return userInfoMap;
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
