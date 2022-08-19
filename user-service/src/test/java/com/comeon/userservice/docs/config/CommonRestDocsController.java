package com.comeon.userservice.docs.config;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.common.response.ApiResponseCode;
import com.comeon.userservice.web.common.response.ErrorCode;
import com.comeon.userservice.web.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class CommonRestDocsController {

    @GetMapping("/docs/success")
    public ApiResponse<?> commonResponseFields() {
        Map<String, String> responseCodes = Arrays.stream(ApiResponseCode.values())
                .collect(Collectors.toMap(ApiResponseCode::getId, ApiResponseCode::getText));

        return ApiResponse.createSuccess(responseCodes);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> commonErrorResponseFields() {
        return ApiResponse.createBadParameter(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생하였습니다.");
    }

    @GetMapping("/docs/error/codes")
    public ApiResponse<?> errorResponseCodes() {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getDescription));
        return ApiResponse.createSuccess(errorCodes);
    }

    @GetMapping("/docs/providers")
    public ApiResponse<?> providerCods() {
        Map<String, String> providers = Arrays.stream(OAuthProvider.values())
                .collect(Collectors.toMap(OAuthProvider::getValue, OAuthProvider::getDescription));
        return ApiResponse.createSuccess(providers);
    }
}
