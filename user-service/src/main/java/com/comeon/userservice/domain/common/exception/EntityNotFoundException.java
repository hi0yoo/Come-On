package com.comeon.userservice.domain.common.exception;

import java.util.NoSuchElementException;

public class EntityNotFoundException extends NoSuchElementException {

    private static final String message = "존재하지 않는 데이터입니다.";

    public EntityNotFoundException() {
        super(message);
    }
}
