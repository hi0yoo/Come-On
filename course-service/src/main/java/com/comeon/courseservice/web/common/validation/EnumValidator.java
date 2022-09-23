package com.comeon.courseservice.web.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private ValidEnum annotation;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (annotation.nullable() && Objects.isNull(value)) {
            return true;
        }

        Enum<?>[] enumConstants = annotation.enumClass().getEnumConstants();
        if (Objects.nonNull(value) && Objects.nonNull(enumConstants)) {
            for (Enum<?> enumConstant : enumConstants) {
                if (value.equals(enumConstant.name())
                        || (this.annotation.ignoreCase()
                        && value.equalsIgnoreCase(enumConstant.name()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
