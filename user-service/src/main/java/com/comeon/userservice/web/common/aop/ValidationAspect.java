package com.comeon.userservice.web.common.aop;

import com.comeon.userservice.common.exception.CustomException;
import com.comeon.userservice.common.exception.ErrorCode;
import com.comeon.userservice.web.common.exception.ValidateException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Locale;

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
                                    messageSource.getMessage(fieldError, Locale.getDefault())
                            )
                    );

            bindingResult.getGlobalErrors()
                    .forEach(objectError -> errorResult.add(
                                    "Global",
                                    messageSource.getMessage(objectError, Locale.getDefault())
                            )
                    );

            throw new ValidateException("요청 데이터 검증에 실패하였습니다.\n" + errorResult, errorResult);
        }
    }
}
