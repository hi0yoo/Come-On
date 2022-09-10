package com.comeon.courseservice.web.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(Enum value, ConstraintValidatorContext context) {
        if (annotation.nullable() && Objects.isNull(value)) {
            return true;
        }

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
