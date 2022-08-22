package com.comeon.meetingservice.web.common.util;

import com.comeon.meetingservice.web.common.exception.ValidationFailException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;

public class ValidationUtils {

    public static void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
            bindingResult.getGlobalErrors().stream()
                    .forEach(oe -> errorMap.add("objectError", oe.getDefaultMessage()));

            bindingResult.getFieldErrors().stream()
                    .forEach(fe -> errorMap.add(fe.getField(), fe.getDefaultMessage()));

            throw new ValidationFailException("요청 데이터 검증 실패: \n" + errorMap.toString(), errorMap);
        }
    }
}