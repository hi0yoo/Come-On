package com.comeon.userservice.web.common.exception.resolver;

import com.comeon.userservice.domain.common.exception.EntityNotFoundException;
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
        //  현재 방식은 아무리 생각해도 별로다...
        //  예외 발생시, 메시지에 bindingResult를 사용하여 예외 메시지를 생성하고,
        //  생성된 예외 메시지를 API 응답으로 하는게 Best 인가..
        //  조금 더 고민하다가 User-Service 마무리 전에 수정하겠습니다.
        MultiValueMap<String, String> errorMessage = new LinkedMultiValueMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorMessage.add(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ApiResponse.createBadParameter(ErrorCode.VALIDATE_ERROR, errorMessage.toString());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ApiResponse<ErrorResponse> entityNotFoundExceptionHandle(EntityNotFoundException e) {
        log.error("[EntityNotFoundException]", e);

        return ApiResponse.createBadParameter(ErrorCode.ENTITY_NOT_FOUND, e.getMessage());
    }
}
