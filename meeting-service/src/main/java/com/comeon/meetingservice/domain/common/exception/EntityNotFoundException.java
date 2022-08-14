package com.comeon.meetingservice.domain.common.exception;

public class EntityNotFoundException extends IllegalArgumentException {

    public EntityNotFoundException(String s) {
        super(s);
    }
}
