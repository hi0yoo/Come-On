package com.comeon.meetingservice.web.common.util;

import com.comeon.meetingservice.web.common.exception.ValidationFailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.*;

import java.util.Arrays;
import java.util.Objects;

@Component
public class ValidationUtils {

    private final MessageCodesResolver codesResolver;
    private final MessageSource messageSource;

    @Autowired
    public ValidationUtils(MessageSource messageSource) {
        this.codesResolver = new DefaultMessageCodesResolver();
        this.messageSource = messageSource;
    }

    public void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
            bindingResult.getGlobalErrors().stream()
                    .forEach(oe -> errorMap.add("objectError", getErrorMessage(oe)));
            bindingResult.getFieldErrors().stream()
                    .forEach(fe -> errorMap.add(fe.getField(), getErrorMessage(fe)));

            throw new ValidationFailException("요청 데이터 검증 실패: \n" + errorMap.toString(), errorMap);
        }
    }

    // 우선순위: validation.properties(level1 ~ 4) -> default message
    private String getErrorMessage(ObjectError objectError) {
        return Arrays.stream(codesResolver.resolveMessageCodes(
                        objectError.getCode(),
                        objectError.getObjectName()))
                .filter(s -> Objects.nonNull(getMessage(s, objectError)))
                .map(s -> getMessage(s, objectError))
                .findFirst().orElse(objectError.getDefaultMessage());
    }

    private String getErrorMessage(FieldError fieldError) {
        return Arrays.stream(codesResolver.resolveMessageCodes(
                        fieldError.getCode(),
                        fieldError.getObjectName(),
                        fieldError.getField(),
                        fieldError.getField().getClass()))
                .filter(s -> Objects.nonNull(getMessage(s, fieldError)))
                .map(s -> getMessage(s, fieldError))
                .findFirst().orElse(fieldError.getDefaultMessage());
    }

    private String getMessage(String s, ObjectError objectError) {
        try {
            return messageSource.getMessage(s, objectError.getArguments(), null);
        } catch (NoSuchMessageException e) {
            return null;
        }
    }
}