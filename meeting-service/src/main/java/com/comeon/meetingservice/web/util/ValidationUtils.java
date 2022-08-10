package com.comeon.meetingservice.web.util;

import com.comeon.meetingservice.web.exception.ValidationFailException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValidationUtils {

    public void validate(BindingResult bindingResult) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

            System.out.println(new ObjectMapper().writeValueAsString(errorMap));

            throw new ValidationFailException(
                    new ObjectMapper().writeValueAsString(errorMap));
        }
    }
}