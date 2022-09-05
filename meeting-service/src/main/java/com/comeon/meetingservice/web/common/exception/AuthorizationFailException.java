package com.comeon.meetingservice.web.common.exception;

import com.comeon.meetingservice.common.exception.ErrorCode;

public class AuthorizationFailException extends IllegalArgumentException {

    private final ErrorCode errorCode;

    public AuthorizationFailException(String s) {
        super(s);
        this.errorCode = ErrorCode.AUTHORIZATION_FAIL;
    }
}
