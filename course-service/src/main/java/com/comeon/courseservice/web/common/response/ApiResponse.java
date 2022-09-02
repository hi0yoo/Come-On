package com.comeon.courseservice.web.common.response;

import com.comeon.courseservice.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private LocalDateTime responseTime;

    private ApiResponseCode code;

    private T data;

    public static <T> ApiResponse<T> createSuccess() {
        return ApiResponse.<T>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SUCCESS)
                .build();
    }

    public static <T> ApiResponse<T> createSuccess(T data) {
        return ApiResponse.<T>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SUCCESS)
                .data(data)
                .build();
    }

    public static ApiResponse<ErrorResponse> createBadParameter(ErrorCode errorCode) {
        ErrorResponse errorResponse = createErrorResponse(errorCode);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createBadParameter(ErrorCode errorCode, MultiValueMap<String, String> errorResult) {
        ErrorResponse errorResponse = createValidateErrorResponse(errorCode, errorResult);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(ErrorCode errorCode) {
        ErrorResponse errorResponse = createErrorResponse(errorCode);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(ErrorCode errorCode) {
        ErrorResponse errorResponse = createErrorResponse(errorCode);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(ErrorCode errorCode) {
        ErrorResponse errorResponse = createErrorResponse(errorCode);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(errorResponse)
                .build();
    }

    private static ErrorResponse createErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    private static ErrorResponse createValidateErrorResponse(ErrorCode errorCode, MultiValueMap<String, String> errorResult) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorResult)
                .build();
    }

    public static ApiResponse<ErrorResponse> createError(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.getResponseCode(errorCode.getHttpStatus()))
                .data(createErrorResponse(errorCode))
                .build();
    }
}
