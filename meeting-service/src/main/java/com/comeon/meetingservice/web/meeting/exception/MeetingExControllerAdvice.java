package com.comeon.meetingservice.web.meeting.exception;

import com.comeon.meetingservice.domain.meeting.exception.ImageFileNotIncludeException;
import com.comeon.meetingservice.web.common.response.ApiResponse;
import com.comeon.meetingservice.web.meeting.MeetingController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RestControllerAdvice(assignableTypes = {MeetingController.class})
public class MeetingExControllerAdvice {

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse imageFileNotIncludeExHandler(ImageFileNotIncludeException e) {
        log.error("[ImageFileNotIncludeException]", e);
        return ApiResponse.createBadParameter("102", e.getMessage());
    }

}
