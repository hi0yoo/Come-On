package com.comeon.userservice.web.common.aop;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.web.common.exception.ValidateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Locale;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ValidationAspect {

    private final MessageSource messageSource;

    @Pointcut("execution(* com.comeon.userservice.web..*.*(..))")
    public void webPackagePointcut() {

    }

    @Before("webPackagePointcut() && @annotation(annotation)")
    public void validate(JoinPoint joinPoint, ValidationRequired annotation) {
        BindingResult bindingResult = Arrays.stream(joinPoint.getArgs())
                .filter(BindingResult.class::isInstance)
                .map(BindingResult.class::cast)
                .findFirst()
                .orElseThrow(
                        () -> new CustomException(
                                "BindingResult가 없습니다. Target Class : " + joinPoint.getTarget().getClass().getSimpleName(),
                                ErrorCode.SERVER_ERROR
                        )
                );

        if (bindingResult.hasErrors()) {
            LinkedMultiValueMap<String, String> errorResult = new LinkedMultiValueMap<>();

            bindingResult.getFieldErrors()
                    .forEach(fieldError -> errorResult.add(
                                    fieldError.getField(),
                                    getMessage(fieldError)
                            )
                    );

            bindingResult.getGlobalErrors()
                    .forEach(objectError -> errorResult.add(
                                    "Global",
                                    getMessage(objectError)
                            )
                    );

            throw new ValidateException("요청 데이터 검증에 실패하였습니다.\n" + errorResult, errorResult);
        }
    }

    private String getMessage(MessageSourceResolvable resolvable) {
        try {
            return messageSource.getMessage(resolvable, Locale.getDefault());
        } catch (NoSuchMessageException e) {
            ObjectError error = (ObjectError) resolvable;
            log.warn("[{}] 코드와 매칭되는 메시지가 없습니다. Code : {}", e.getClass().getSimpleName(), error.getCode(), e);
            return null;
        }
    }
}
