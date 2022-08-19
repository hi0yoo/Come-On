package com.comeon.userservice.web.common.exception.resolver;

import com.comeon.userservice.web.common.exception.ValidateException;
import com.comeon.userservice.web.common.response.ApiResponse;
import com.comeon.userservice.web.common.response.ErrorCode;
import com.comeon.userservice.web.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.comeon.userservice.web")
public class CommonControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse<ErrorResponse> validateExceptionHandle(ValidateException e) {
        log.error("[ValidateException]", e);

        // TODO 메시지 처리 수정
        MultiValueMap<String, String> errorMessage = new LinkedMultiValueMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMessage.add(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ApiResponse.createBadParameter(ErrorCode.VALIDATE_ERROR, errorMessage.toString());
    }
}
