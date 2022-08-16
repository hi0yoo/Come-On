package com.comeon.meetingservice.web.common.exception;

public class ValidationFailException extends IllegalArgumentException {

    public ValidationFailException(String s) {
        super(s);
    }
}
