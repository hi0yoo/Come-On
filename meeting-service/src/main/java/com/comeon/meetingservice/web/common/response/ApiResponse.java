package com.comeon.meetingservice.web.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;
import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class ApiResponse<T> {

    private LocalDateTime responseTime;

    private ApiResponseCode code;

    private T data;

    public static <T> ApiResponse<T> createSuccess(T data) {
        return ApiResponse.<T>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SUCCESS)
                .data(data)
                .build();
    }

    public static ApiResponse<ErrorResponse> createBadParameter(String errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode, message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(String errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode, message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(String errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode, message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(String errorCode, String message) {
        ErrorResponse errorResponse = createErrorResponse(errorCode, message);

        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(errorResponse)
                .build();
    }

    private static ErrorResponse createErrorResponse(String errorCode, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode)
                .message(message)
                .build();
        return errorResponse;
    }



}
