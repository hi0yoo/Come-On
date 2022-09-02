package com.comeon.meetingservice.web.common.aop;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.web.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ValidationAspect {

    private final ValidationUtils validationUtils;

    @Pointcut("execution(* com.comeon.meetingservice.web..*.*(..))")
    public void allWeb() {

    }

    @Before("allWeb() && @annotation(com.comeon.meetingservice.web.common.aop.ValidationRequired)")
    public void validate(JoinPoint joinPoint) {
        log.info("AOP IN");
        BindingResult bindingResult = Arrays.stream(joinPoint.getArgs())
                .filter(BindingResult.class::isInstance)
                .map(BindingResult.class::cast)
                .findFirst()
                .orElseThrow(() -> new CustomException("BindingResult가 선언되어 있지 않습니다.",
                        ErrorCode.BINDING_RESULT_NOT_FOUND));

        validationUtils.validate(bindingResult);
    }
}
