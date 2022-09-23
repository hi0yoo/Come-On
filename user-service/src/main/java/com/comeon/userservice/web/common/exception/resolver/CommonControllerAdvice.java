package com.comeon.userservice.web.common.exception.resolver;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
import com.comeon.userservice.web.common.exception.ValidateException;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.comeon.userservice.web")
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
                .body(ApiResponse.createError(errorCode));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> entityNotFoundExceptionHandle(EntityNotFoundException e) {
        log.error("[{}]", e.getClass(), e);

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createError(errorCode));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> missingServletRequestParameterExceptionHandle(MissingServletRequestParameterException e) {
        log.error("[{}]", e.getClass(), e);
        MultiValueMap<String, String> errorResult = new LinkedMultiValueMap<>();
        errorResult.add(e.getParameterName(), "파라미터가 비어있으면 안됩니다.");


        ErrorCode errorCode = ErrorCode.VALIDATION_FAIL;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createBadParameter(errorCode, errorResult));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<ErrorResponse>> httpMessageNotReadableExceptionHandle(HttpMessageNotReadableException e) {
        log.error("[{}] {}", e.getClass().getSimpleName(), e.getMessage(), e);
        ErrorCode errorCode = ErrorCode.BAD_REQUEST_DATA;
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.createBadParameter(errorCode));
    }
}
