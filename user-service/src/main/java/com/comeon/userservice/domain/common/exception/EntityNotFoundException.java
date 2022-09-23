package com.comeon.userservice.domain.common.exception;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;

public class EntityNotFoundException extends CustomException {

    private static final ErrorCode ERROR_CODE_ENTITY_NOT_FOUND = ErrorCode.ENTITY_NOT_FOUND;

    public EntityNotFoundException() {
        super(ERROR_CODE_ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, ERROR_CODE_ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR_CODE_ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause, ERROR_CODE_ENTITY_NOT_FOUND);
    }
}
