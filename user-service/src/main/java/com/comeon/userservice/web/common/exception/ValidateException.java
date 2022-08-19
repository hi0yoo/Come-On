package com.comeon.userservice.web.common.exception;

import org.springframework.validation.BindingResult;

public class ValidateException extends RuntimeException {

    private final BindingResult bindingResult;

    public ValidateException(BindingResult bindingResult) {
        super(bindingResult.toString());
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
