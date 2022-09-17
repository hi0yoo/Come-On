package com.comeon.meetingservice.web.common.response;

import com.comeon.meetingservice.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)

public class ApiResponse<T> {

    private LocalDateTime responseTime;

    private ApiResponseCode code;

    private T data;

    public static <T> ApiResponse<T> createCustom(ApiResponseCode apiResponseCode, T data) {
        return ApiResponse.<T> builder()
                .responseTime(LocalDateTime.now())
                .code(apiResponseCode)
                .data(data)
                .build();
    }

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

    public static ApiResponse<ErrorResponse> createError(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.getResponseCode(errorCode.getHttpStatus()))
                .data(createErrorResponse(errorCode))
                .build();
    }

    public static ApiResponse<ErrorResponse> createBadParameter(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(createErrorResponse(errorCode))
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(createErrorResponse(errorCode))
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(createErrorResponse(errorCode))
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(ErrorCode errorCode) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(createErrorResponse(errorCode))
                .build();
    }

    private static ErrorResponse createErrorResponse(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

}
