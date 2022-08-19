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

    public static ApiResponse<ErrorResponse> createBadParameter(Throwable exception) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(createErrorResponse(exception))
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(Throwable exception) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(createErrorResponse(exception))
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(Throwable exception) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(createErrorResponse(exception))
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(Throwable exception) {
        return ApiResponse.<ErrorResponse>builder()
                .responseTime(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(createErrorResponse(exception))
                .build();
    }

    private static ErrorResponse createErrorResponse(Throwable exception) {
        return ErrorResponse.builder()
                .code(ErrorCode.findCode(exception))
                .message(exception.getMessage())
                .build();
    }

}
