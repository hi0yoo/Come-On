package com.comeon.meetingservice.web.common.util;

import com.comeon.meetingservice.web.common.exception.ValidationFailException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Map;
import java.util.stream.Collectors;

public class ValidationUtils {

    public static void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            MultiValueMap<String, String> errorMap = new LinkedMultiValueMap<>();
            bindingResult.getGlobalErrors().stream()
                    .forEach(oe -> errorMap.add("Object Error", oe.getDefaultMessage()));

            bindingResult.getFieldErrors().stream()
                    .forEach(fe -> errorMap.add(fe.getField(), fe.getDefaultMessage()));

            throw new ValidationFailException(errorMap.toString());
        }
    }
}