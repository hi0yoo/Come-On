package com.comeon.authservice.web.auth.exception.handler;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ErrorCode;
import com.comeon.authservice.web.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.comeon.authservice.web.auth.controller")
public class AuthControllerExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> accessTokenNotExpiredExceptionHandle(AccessTokenNotExpiredException e) {
        return ApiResponse.createBadParameter(ErrorCode.createErrorCode(e));
    }
}
