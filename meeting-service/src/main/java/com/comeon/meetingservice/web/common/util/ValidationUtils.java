package com.comeon.meetingservice.web.common.util;

import com.comeon.meetingservice.web.common.exception.ValidationFailException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.*;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ValidationUtils {

    private final MessageSource messageSource;

    public void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();

            bindingResult.getGlobalErrors().stream()
                    .forEach(oe -> errorMap.add("objectError", getMessage(oe)));

            bindingResult.getFieldErrors().stream()
                    .forEach(fe -> errorMap.add(fe.getField(), getMessage(fe)));

            throw new ValidationFailException("요청 데이터 검증 실패: \n" + errorMap, errorMap);
        }
    }

    private String getMessage(MessageSourceResolvable error) {
        try {
            return messageSource.getMessage(error, Locale.getDefault());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }
}