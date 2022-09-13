package com.comeon.meetingservice.web.common.feign.userservice;

import com.comeon.meetingservice.web.common.feign.userservice.response.UserListResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceApiResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserServiceListResponse;
import com.comeon.meetingservice.web.common.feign.userservice.response.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFeignService {

    private final UserServiceFeignClient userServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    // User Service와 통신하여 id: userInfo 형식의 Map으로 정보를 변환해주는 메서드
    public Map<Long, UserListResponse> getUserInfoMap(List<Long> userIds) {
        CircuitBreaker userListCb = circuitBreakerFactory.create("userList");
        UserServiceApiResponse<UserServiceListResponse<UserListResponse>> userResponse
                = userListCb.run(() -> userServiceFeignClient.getUsers(userIds),
                throwable -> {
                    log.error("[User Service Error]", throwable);
                    return null;
                });

        if (Objects.nonNull(userResponse)) {
            // 응답이 왔다면, 받아온 회원 정보를 id: response 형식의 Map으로 만들기
            return userResponse.getData().getContents().stream()
                    .collect(Collectors.toMap(UserListResponse::getUserId, ul -> ul));
        } else {
            // UserService가 장애가 발생하여 응답이 없다면 우선 id 필드만 있는 ListResponse를 넣어 반환 (NPE에 취약할 수 있기 때문)
            return userIds.stream()
                    .collect(Collectors.toMap(id -> id,
                            id -> UserListResponse.builder().userId(id).build()));
        }
    }
}
