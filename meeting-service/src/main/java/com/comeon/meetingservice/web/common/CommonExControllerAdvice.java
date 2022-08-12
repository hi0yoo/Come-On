package com.comeon.meetingservice.web.common;

import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.exception.ValidationFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice
public class CommonExControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse validationFailExHandler(ValidationFailException e) {
        log.error("[ValidationFailException]", e);
        return ApiResponse.createBadParameter("101", e.getMessage());
    }
}
