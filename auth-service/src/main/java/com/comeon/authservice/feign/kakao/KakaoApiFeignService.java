package com.comeon.authservice.feign.kakao;

import com.comeon.authservice.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiFeignService {

    private static final String KAKAO_AUTHORIZATION_HEADER_PREFIX = "KakaoAk ";

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    private final KakaoApiFeignClient kakaoApiFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public void userUnlink(Long targetId) {
        CircuitBreaker userUnlinkCb = circuitBreakerFactory.create("userUnlink");
        userUnlinkCb.run(
                () -> kakaoApiFeignClient.userUnlink(
                        KAKAO_AUTHORIZATION_HEADER_PREFIX + kakaoAdminKey,
                        targetId,
                        "user_id"
                ),
                throwable -> { throw (CustomException) throwable; }
        );
    }
}
