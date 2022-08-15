package com.comeon.meetingservice.web.common.exception;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class CommonExControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse validationFailExHandler(ValidationFailException e) {
        log.error("[ValidationFailException]", e);
        return ApiResponse.createBadParameter("101", e.getMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse EmptyFileExHandler(EmptyFileException e) {
        log.error("[EmptyFileException]", e);
        return ApiResponse.createBadParameter("102", e.getMessage());
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ApiResponse UploadFailExHandler(UploadFailException e) {
        log.error("[UploadFailException]", e);
        return ApiResponse.createServerError("103", e.getMessage());
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse EntityNotFoundExHandler(EntityNotFoundException e) {
        log.error("[EntityNotFoundException]", e);
        return ApiResponse.createBadParameter("104", e.getMessage());
    }

}
