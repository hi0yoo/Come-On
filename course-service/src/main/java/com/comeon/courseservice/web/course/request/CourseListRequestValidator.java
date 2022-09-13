package com.comeon.courseservice.web.course.request;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
public class CourseListRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CourseListRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CourseListRequest request = (CourseListRequest) target;

        Double lat = request.getLat();
        Double lng = request.getLng();

        if (!(isCoordinateNull(lat, lng) || isCoordinateNonNull(lat, lng))) {
            errors.reject("Coordinate", "위도와 경도를 모두 입력하거나, 모두 입력하지 않아야 합니다.");
        }
    }

    private boolean isCoordinateNonNull(Double lat, Double lng) {
        return Objects.nonNull(lat) && Objects.nonNull(lng);
    }

    private boolean isCoordinateNull(Double lat, Double lng) {
        return Objects.isNull(lat) && Objects.isNull(lng);
    }
}
