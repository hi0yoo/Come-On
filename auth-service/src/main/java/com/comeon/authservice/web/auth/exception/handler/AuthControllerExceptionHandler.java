package com.comeon.authservice.web.auth.exception.handler;

import com.comeon.authservice.auth.jwt.exception.AccessTokenNotExpiredException;
import com.comeon.authservice.auth.jwt.exception.InvalidJwtException;
import com.comeon.authservice.auth.jwt.exception.JwtNotExistException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.comeon.authservice.web.auth.controller")
public class AuthControllerExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse accessTokenNotExpiredExceptionHandle(AccessTokenNotExpiredException e) {
        return new ErrorResponse("Jwt Not Expired", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse refreshTokenNotExistExceptionHandle(JwtNotExistException e) {
        return new ErrorResponse("Jwt Not Exist", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse invalidJwtExceptionHandle(InvalidJwtException e) {
        return new ErrorResponse("Invalid Jwt", e.getMessage()) ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse expiredJwtExceptionHandle(ExpiredJwtException e) {
        System.out.println(e.getMessage());
        return new ErrorResponse("Jwt Expired", e.getMessage());
    }
}
