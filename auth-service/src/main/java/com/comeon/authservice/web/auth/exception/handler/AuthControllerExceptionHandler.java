package com.comeon.authservice.web.auth.exception.handler;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.auth.jwt.exception.InvalidJwtException;
import com.comeon.authservice.auth.jwt.exception.JwtNotExistException;
import com.comeon.authservice.web.common.response.ApiResponse;
import com.comeon.authservice.web.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.comeon.authservice.web.auth.controller")
public class AuthControllerExceptionHandler {

    // TODO error code 지정

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> accessTokenNotExpiredExceptionHandle(AccessTokenNotExpiredException e) {
        return ApiResponse.createBadParameter("요청 거부 코드", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> jwtNotExistExceptionHandle(JwtNotExistException e) {
        return ApiResponse.createBadParameter("토큰 없음 코드", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<ErrorResponse> invalidJwtExceptionHandle(InvalidJwtException e) {
        return ApiResponse.createUnauthorized("유효하지 않은 토큰 코드", e.getMessage());
    }

}
