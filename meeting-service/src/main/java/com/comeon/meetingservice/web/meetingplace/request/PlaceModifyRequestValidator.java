package com.comeon.meetingservice.web.meetingplace.request;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
public class PlaceModifyRequestValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return MeetingPlaceModifyRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MeetingPlaceModifyRequest meetingPlaceModifyRequest = (MeetingPlaceModifyRequest) target;

        // 3개 중 하나라도 null이 아닐 때,
        if (Objects.nonNull(meetingPlaceModifyRequest.getName())
                || Objects.nonNull(meetingPlaceModifyRequest.getLat())
                || Objects.nonNull(meetingPlaceModifyRequest.getLng())) {

            // 3개 중 하나라도 null인 필드가 있다면 예외
            if (Objects.isNull(meetingPlaceModifyRequest.getName())
                    || Objects.isNull(meetingPlaceModifyRequest.getLat())
                    || Objects.isNull(meetingPlaceModifyRequest.getLng())) {
                errors.reject("requiredAll",
                        "장소 정보를 수정하기 위해서는 name, lat, lng 3 필드 모두 필요합니다.");
            }
        }

        if (Objects.isNull(meetingPlaceModifyRequest.getName())
                && Objects.isNull(meetingPlaceModifyRequest.getLat())
                && Objects.isNull(meetingPlaceModifyRequest.getLng())
                && Objects.isNull(meetingPlaceModifyRequest.getMemo())
                && Objects.isNull(meetingPlaceModifyRequest.getOrder())) {
            errors.reject("noModifyingData", "수정하려는 필드가 한 개도 없습니다.");
        }

    }
}
