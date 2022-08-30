package com.comeon.courseservice.web.common.exception.resolver;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.web.common.exception.ValidateException;
import com.comeon.courseservice.web.common.response.ApiResponse;
import com.comeon.courseservice.web.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.comeon.courseservice.web")
public class CommonControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> validateExceptionHandle(ValidateException e) {
        log.error("[{}]", e.getClass(), e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createBadParameter(errorCode, e.getErrorResult()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> customExceptionHandle(CustomException e) {
        log.error("[{}]", e.getClass(), e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createServerError(errorCode));
    }
}
