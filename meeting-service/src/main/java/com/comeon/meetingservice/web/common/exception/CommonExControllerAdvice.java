package com.comeon.meetingservice.web.common.exception;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class CommonExControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> customExceptionHandler(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("CustomException [{}] = {}", errorCode.getName(), e.getMessage(), e);
        return new ResponseEntity<>(
                ApiResponse.createError(errorCode), errorCode.getHttpStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> validationFailExHandler(ValidationFailException e) {
        log.error("[ValidationFailException]", e);

        ErrorResponse<MultiValueMap<String, String>> errorResponse =
                ErrorResponse.<MultiValueMap<String, String>>builder()
                        .code(e.getErrorCode().getCode())
                        .message(e.getErrorMap())
                        .build();
        return ApiResponse.createCustom(ApiResponseCode.BAD_PARAMETER, errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> httpMessageNotReadableExHandler(HttpMessageNotReadableException e) {
        log.error("[HttpMessageNotReadableException]", e);

        ErrorResponse<String> errorResponse =
                ErrorResponse.<String>builder()
                        .code(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getCode())
                        .message(ErrorCode.HTTP_MESSAGE_NOT_READABLE.getMessage())
                        .build();
        return ApiResponse.createCustom(ApiResponseCode.BAD_PARAMETER, errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<ErrorResponse> authorizationFailExHandler(AuthorizationFailException e) {
        log.error("[AuthorizationFailException]", e);

        ErrorResponse<String> errorResponse =
                ErrorResponse.<String>builder()
                        .code(ErrorCode.AUTHORIZATION_FAIL.getCode())
                        .message(e.getMessage())
                        .build();
        return ApiResponse.createCustom(ApiResponseCode.FORBIDDEN, errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<ErrorResponse> unspecifiedExHandler(Exception e) {
        log.error("[Unspecified Exception] ExClass: {} \n", e.getClass().getSimpleName(), e);

        ErrorResponse<String> errorResponse =
                ErrorResponse.<String>builder()
                        .code(ErrorCode.UNSPECIFIED_ERROR.getCode())
                        .message(ErrorCode.UNSPECIFIED_ERROR.getMessage())
                        .build();
        return ApiResponse.createCustom(ApiResponseCode.FORBIDDEN, errorResponse);
    }

}
