package com.comeon.authservice.web.common.exception.resolver;

import com.comeon.authservice.common.exception.CustomException;
import com.comeon.authservice.common.exception.ErrorCode;
import com.comeon.authservice.common.response.ApiResponse;
import com.comeon.authservice.common.response.ErrorResponse;
import com.comeon.authservice.web.common.exception.ValidateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.comeon.authservice.web")
public class CommonControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> validateExceptionHandle(ValidateException e) {
        log.error("[{}]", e.getClass().getSimpleName(), e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createBadParameter(errorCode, e.getErrorResult()));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> customExceptionHandle(CustomException e) {
        log.error("[{}]", e.getClass().getSimpleName(), e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createError(errorCode));
    }
}
