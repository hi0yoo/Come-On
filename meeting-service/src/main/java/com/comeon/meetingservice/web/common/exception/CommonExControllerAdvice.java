package com.comeon.meetingservice.web.common.exception;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.common.response.ApiResponseCode;
import com.comeon.meetingservice.web.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonExControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> CustomExceptionHandler(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[{}] = {} \n {}", errorCode.getName(), e.getMessage(), e.getStackTrace());
        return new ResponseEntity<>(
                ApiResponse.createBadParameter(errorCode), errorCode.getHttpStatus());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> ValidationFailExHandler(ValidationFailException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("[{}] = {} \n {}", errorCode.getName(), e.getMessage(), e.getStackTrace());

        ErrorResponse<MultiValueMap<String, String>> errorResponse =
                ErrorResponse.<MultiValueMap<String, String>>builder()
                        .code(e.getErrorCode().getCode())
                        .message(e.getErrorMap())
                        .build();
        return ApiResponse.createCustom(ApiResponseCode.BAD_PARAMETER, errorResponse);
    }

}
