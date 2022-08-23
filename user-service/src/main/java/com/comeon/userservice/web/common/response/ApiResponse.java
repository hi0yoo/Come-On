package com.comeon.userservice.web.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

    public static ApiResponse<ErrorResponse> createBadParameter(ErrorCode errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode.getCode(), message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(ErrorCode errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode.getCode(), message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(ErrorCode errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode.getCode(), message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(ErrorCode errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode.getCode(), message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(errorResponse)
                .build();
    }

    private static ErrorResponse createErrorResponse(Integer code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }
}