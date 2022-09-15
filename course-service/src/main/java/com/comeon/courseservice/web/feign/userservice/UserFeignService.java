package com.comeon.courseservice.web.feign.userservice;

import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.feign.userservice.response.ListResponse;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserFeignService {

    private final UserServiceFeignClient userServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public Map<Long, UserDetailsResponse> getUserDetailsMap(List<Long> userIds) {
        CircuitBreaker getUserDetailsMapCb = circuitBreakerFactory.create("getUserDetailsMap");
        ApiResponse<ListResponse<UserDetailsResponse>> userDetailsListApiResponse =
                getUserDetailsMapCb.run(
                        () -> userServiceFeignClient.getUserDetailsList(userIds),
                        throwable -> {
                            log.error("[User-Service Error]", throwable);
                            return null;
                        }
                );

        if (Objects.isNull(userDetailsListApiResponse)) {
            return new HashMap<>();
        }

        return userDetailsListApiResponse.getData()
                .getContents()
                .stream()
                .collect(Collectors.toMap(
                                UserDetailsResponse::getUserId,
                                userDetailsResponse -> userDetailsResponse
                        )
                );
    }

    public Optional<UserDetailsResponse> getUserDetails(Long userId) {
        CircuitBreaker getUserDetailsMapCb = circuitBreakerFactory.create("getUserDetails");
        ApiResponse<UserDetailsResponse> userDetailsApiResponse =
                getUserDetailsMapCb.run(
                        () -> userServiceFeignClient.getUserDetails(userId),
                        throwable -> {
                            log.error("[User-Service Error]", throwable);
                            return null;
                        }
                );

        if (Objects.nonNull(userDetailsApiResponse)) {
            return Optional.of(userDetailsApiResponse.getData());
        }
        return Optional.empty();
    }
}
