package com.comeon.userservice.web.feign;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.web.common.response.ErrorResponse;
import com.comeon.userservice.web.feign.authservice.response.AuthServiceApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            AuthServiceApiResponse<ErrorResponse> authServiceApiResponse =
                    objectMapper.readValue(
                            response.body().asInputStream(),
                            new TypeReference<>() {}
                    );

            ErrorResponse errorResponse = authServiceApiResponse.getData();
            switch (errorResponse.getErrorCode()) {
                case 681: // 요청 데이터 검증 실패
                    throw new CustomException("oauthId가 없습니다.", ErrorCode.SERVER_ERROR);
                case 682: // 유효하지 않은 oauthId
                    throw new CustomException("유효하지 않은 oauthId 입니다.", ErrorCode.SERVER_ERROR);
                case 699: // 카카오 API 이용이 불가능합니다
                    throw new CustomException("카카오 API 이용이 불가능합니다.", ErrorCode.KAKAO_API_ERROR);
                default: // Auth-Service 내부 오류 및 나머지 오류
                    throw new CustomException("Auth Service 이용 불가", ErrorCode.AUTH_SERVICE_ERROR);
            }
        } catch (IOException e) {
            throw new CustomException(e, ErrorCode.SERVER_ERROR);
        }
    }
}
