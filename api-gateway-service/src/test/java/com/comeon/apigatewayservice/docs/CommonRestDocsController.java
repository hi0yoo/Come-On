package com.comeon.apigatewayservice.docs;

import com.comeon.apigatewayservice.common.exception.ErrorCode;
import com.comeon.apigatewayservice.common.response.ApiResponse;
import com.comeon.apigatewayservice.common.response.ApiResponseCode;
import com.comeon.apigatewayservice.common.response.ErrorResponse;
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
                .collect(Collectors.toMap(ApiResponseCode::name, ApiResponseCode::getMessage));

        return ApiResponse.createSuccess(responseCodes);
    }

    @GetMapping("/docs/error/codes")
    public ApiResponse<?> errorResponseCodes() {
        Map<Integer, String> errorCodes = Arrays.stream(ErrorCode.values())
                .collect(Collectors.toMap(ErrorCode::getCode, ErrorCode::getMessage));
        return ApiResponse.createSuccess(errorCodes);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @GetMapping("/docs/error")
    public ApiResponse<ErrorResponse> commonErrorResponseFields() {
        return ApiResponse.createServerError(ErrorCode.SERVER_ERROR);
    }
}
