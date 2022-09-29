package com.comeon.userservice.web.feign.authservice;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthFeignService {

    private static final String BEARER_TOKEN_TYPE = "Bearer ";

    private final AuthServiceFeignClient authServiceFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public void userUnlink(String accessToken, Long userOauthId) {
        CircuitBreaker userUnlinkCb = circuitBreakerFactory.create("userUnlink");
        userUnlinkCb.run(
                () -> authServiceFeignClient.unlink(BEARER_TOKEN_TYPE + accessToken, userOauthId),
                throwable -> {
                    throw new CustomException(throwable.getMessage(), ErrorCode.AUTH_SERVICE_ERROR);
                }
        );
    }
}
