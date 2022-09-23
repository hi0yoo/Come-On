package com.comeon.meetingservice.web.common.exception;

import com.comeon.meetingservice.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.util.MultiValueMap;

@Getter
public class ValidationFailException extends IllegalArgumentException {

    private final ErrorCode errorCode;
    private final MultiValueMap<String, String> errorMap;

    public ValidationFailException(String s, MultiValueMap<String, String> errorMap) {
        super(s);
        this.errorMap = errorMap;
        this.errorCode = ErrorCode.VALIDATION_FAIL;
    }
}
