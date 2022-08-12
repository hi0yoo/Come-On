package com.comeon.meetingservice.web.exception;

public class ValidationFailException extends IllegalArgumentException {

    public ValidationFailException(String s) {
        super(s);
    }
}
