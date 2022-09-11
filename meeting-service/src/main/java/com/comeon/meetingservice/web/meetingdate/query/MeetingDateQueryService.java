package com.comeon.meetingservice.web.meetingdate.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.feign.userservice.UserListResponse;
import com.comeon.meetingservice.web.common.feign.userservice.UserServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.userservice.UserServiceFeignClient;
import com.comeon.meetingservice.web.common.feign.userservice.UserServiceListResponse;
import com.comeon.meetingservice.web.meetingdate.response.MeetingDateDetailResponse;
import com.comeon.meetingservice.web.meetingdate.response.MeetingDateDetailUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeetingDateQueryService {

    private final MeetingDateQueryRepository meetingDateQueryRepository;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final UserServiceFeignClient userServiceFeignClient;

    public MeetingDateDetailResponse getDetail(Long meetingId, Long id) {
        MeetingDateEntity meetingDateEntity = meetingDateQueryRepository.findByIdFetchDateUser(meetingId, id)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));

        List<MeetingDateDetailUserResponse> meetingDateDetailUserResponses
                = convertUserResponse(meetingDateEntity.getDateUserEntities());

        return MeetingDateDetailResponse.toResponse(meetingDateEntity, meetingDateDetailUserResponses);
    }

    private List<MeetingDateDetailUserResponse> convertUserResponse(List<DateUserEntity> dateUserEntities) {

        // DateUser로 부터 MeetingUser엔티티를 꺼내 UserId 리스트 만들기
        List<Long> userIds = dateUserEntities.stream()
                .map(DateUserEntity::getMeetingUserEntity)
                .map(MeetingUserEntity::getUserId)
                .collect(Collectors.toList());

        // User Service에서 유저 정보들 조회해오기
        Map<Long, UserListResponse> userInfoMap = getUserInfoMap(userIds);

        return dateUserEntities.stream()
                .map(DateUserEntity::getMeetingUserEntity)
                .map(meetingUserEntity -> {

                    // userInfoMap에서 스트림의 요소 userId와 일치하는 요소 꺼내기
                    UserListResponse userInfo = userInfoMap.get(meetingUserEntity.getUserId());

                    String nickname = null;
                    String profileImageUrl = null;

                    if (Objects.nonNull(userInfo)) {
                        nickname = userInfo.getNickname();
                        profileImageUrl = userInfo.getProfileImageUrl();
                    }

                    return MeetingDateDetailUserResponse.toResponse(
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
            // 받아온 회원 정보를 id: response 형식의 Map으로 만들기
            userInfoMap = userResponses.getData().getContents().stream()
                    .collect(Collectors.toMap(UserListResponse::getUserId, ul -> ul));
        }

        return userInfoMap;
    }
}
