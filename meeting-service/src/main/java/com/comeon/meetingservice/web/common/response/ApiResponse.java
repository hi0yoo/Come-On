package com.comeon.meetingservice.web.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static lombok.AccessLevel.*;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private LocalDateTime timeStamp;

    private ApiResponseCode code;

    private T data;

    public static <T> ApiResponse<T> createSuccess(T data) {
        return ApiResponse.<T>builder()
                .timeStamp(LocalDateTime.now())
                .code(ApiResponseCode.SUCCESS)
                .data(data)
                .build();
    }

    public static ApiResponse<ErrorResponse> createBadParameter(String code, String message) {
        ErrorResponse errorResponse = createErrorResponse(code, message);
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());

        return ApiResponse.<ErrorResponse>builder()
                .timeStamp(LocalDateTime.now())
                .code(ApiResponseCode.BAD_PARAMETER)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createNotFound(String code, String message) {
        ErrorResponse errorResponse = createErrorResponse(code, message);
        errorResponse.setStatusCode(HttpStatus.NOT_FOUND.value());

        return ApiResponse.<ErrorResponse>builder()
                .timeStamp(LocalDateTime.now())
                .code(ApiResponseCode.NOT_FOUND)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createServerError(String code, String message) {
        ErrorResponse errorResponse = createErrorResponse(code, message);
        errorResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ApiResponse.<ErrorResponse>builder()
                .timeStamp(LocalDateTime.now())
                .code(ApiResponseCode.SERVER_ERROR)
                .data(errorResponse)
                .build();
    }

    public static ApiResponse<ErrorResponse> createUnauthorized(String code, String message) {
        ErrorResponse errorResponse = createErrorResponse(code, message);
        errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        return ApiResponse.<ErrorResponse>builder()
                .timeStamp(LocalDateTime.now())
                .code(ApiResponseCode.UNAUTHORIZED)
                .data(errorResponse)
                .build();
    }

    private static ErrorResponse createErrorResponse(String code, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
        return errorResponse;
    }



}
