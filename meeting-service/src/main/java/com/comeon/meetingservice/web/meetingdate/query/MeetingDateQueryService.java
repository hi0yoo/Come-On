package com.comeon.meetingservice.web.meetingdate.query;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.feign.userservice.UserFeignService;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserListResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.userservice.UserServiceFeignClient;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceListResponse;
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
    private final UserFeignService userFeignService;

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
        Map<Long, UserListResponse> userInfoMap = userFeignService.getUserInfoMap(userIds);

        return dateUserEntities.stream()
                .map(DateUserEntity::getMeetingUserEntity)
                .map(meetingUserEntity -> {

                    UserListResponse userInfo = userInfoMap.get(meetingUserEntity.getUserId());

                    return MeetingDateDetailUserResponse.toResponse(
                            meetingUserEntity,
                            userInfo.getNickname(),
                            userInfo.getProfileImageUrl());
                })
                .collect(Collectors.toList());
    }
}
