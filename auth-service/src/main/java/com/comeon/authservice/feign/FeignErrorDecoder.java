package com.comeon.authservice.feign;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400: // come-on 애플리케이션에 등록되지 않은 target_id로 unlink 하면 400 Error
                throw new CustomException("애플리케이션에 등록되지 않은 oauthId 입니다.", ErrorCode.INVALID_OAUTH_ID);
            case 401: // KakaoApi 파라미터에 값을 입력하지 않은 경우 401 Error
                throw new CustomException("카카오 인증 헤더 또는 파라미터 값이 입력되지 않았거나, 어드민 키가 유효하지 않습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
            default:
                throw new CustomException("카카오 API 이용이 불가능합니다.", ErrorCode.KAKAO_API_ERROR);
        }
    }
}
