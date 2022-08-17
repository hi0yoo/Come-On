package com.comeon.authservice.docs.config;

import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ApiResponseCode;
import com.comeon.authservice.web.common.response.ErrorCode;
import com.comeon.authservice.web.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.comeon.authservice.web.common.response.ErrorCode.NOT_EXPIRED_ACCESS_TOKEN;

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
        return ApiResponse.createBadRequest(NOT_EXPIRED_ACCESS_TOKEN);
    }

    @GetMapping("/docs/error/codes")
    public ApiResponse<?> errorResponseCodes() {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        return ApiResponse.createSuccess(errorCodes);
    }
}
