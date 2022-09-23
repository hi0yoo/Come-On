package com.comeon.userservice.web.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Enum value, ConstraintValidatorContext context) {
        Enum<?>[] enumConstants = annotation.enumClass().getEnumConstants();
        if (enumConstants != null) {
            for (Enum<?> enumConstant : enumConstants) {
                if (value == enumConstant) {
                    return true;
                }
            }
        }
        return false;
    }
}
