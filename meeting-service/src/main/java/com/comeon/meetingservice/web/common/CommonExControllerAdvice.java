package com.comeon.meetingservice.web.common;

import com.comeon.meetingservice.domain.meeting.exception.ImageFileNotIncludeException;
import com.comeon.meetingservice.web.exception.ValidationFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class CommonExControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse validationFailExHandler(ValidationFailException e) {
        log.error("[ValidationFailException]", e);
        log.error("[ValidationFailException]", e.getMessage());
        return ApiResponse.createError(ErrorResponse.builder()
                .code("101")
                .message(e.getMessage())
                .statusCode(BAD_REQUEST.value()).build());
    }
}
