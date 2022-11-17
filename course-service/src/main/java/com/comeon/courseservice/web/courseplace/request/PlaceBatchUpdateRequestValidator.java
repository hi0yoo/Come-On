package com.comeon.courseservice.web.courseplace.request;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PlaceBatchUpdateRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CoursePlaceBatchUpdateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CoursePlaceBatchUpdateRequest request = (CoursePlaceBatchUpdateRequest) target;

        List<CoursePlaceSaveRequest> saveRequests = request.getToSave();
        List<CoursePlaceModifyRequestForBatch> modifyRequests = request.getToModify();
        List<CoursePlaceDeleteRequest> deleteRequests = request.getToDelete();

        List<Long> coursePlaceIds = new ArrayList<>();
        List<Integer> coursePlaceOrders = new ArrayList<>();

        /*
            toSave, toModify 데이터의 order 값이 하나도 중복되지 않으면 통과
            toModify, toDelete 데이터의 id 값이 하나도 중복되지 않으면 통과
         */

        if (Objects.nonNull(saveRequests)) {
            coursePlaceOrders.addAll(
                    saveRequests.stream()
                            .map(CoursePlaceSaveRequest::getOrder)
                            .collect(Collectors.toList())
            );
        }

        if (Objects.nonNull(modifyRequests)) {
            coursePlaceIds.addAll(
                    modifyRequests.stream()
                            .map(CoursePlaceModifyRequestForBatch::getId)
                            .collect(Collectors.toList())
            );
            coursePlaceOrders.addAll(
                    modifyRequests.stream()
                            .map(CoursePlaceModifyRequestForBatch::getOrder)
                            .collect(Collectors.toList())
            );
        }

        if (Objects.nonNull(deleteRequests)) {
            coursePlaceIds.addAll(
                    deleteRequests.stream()
                            .map(CoursePlaceDeleteRequest::getId)
                            .collect(Collectors.toList())
            );
        }

        // null 제거
        coursePlaceIds.removeIf(Objects::isNull);
        coursePlaceOrders.removeIf(Objects::isNull);

        // 정렬
        coursePlaceIds.sort(Comparator.comparingLong(Long::longValue));
        coursePlaceOrders.sort(Comparator.comparingInt(Integer::intValue));

        // id 중복 검증
        checkCoursePlaceIdDuplicate(errors, coursePlaceIds);

        // order 중복 검증
        checkOrderDuplicate(errors, coursePlaceOrders);
    }

    private void checkCoursePlaceIdDuplicate(Errors errors, List<Long> coursePlaceIds) {
        Set<Long> duplicatePlaceIds = coursePlaceIds.stream()
                .filter(placeId -> Collections.frequency(coursePlaceIds, placeId) > 1)
                .collect(Collectors.toSet());

        if (!duplicatePlaceIds.isEmpty()) {
            errors.reject("Duplicate", new String[]{"coursePlaceId"}, null);
        }
    }

    private void checkOrderDuplicate(Errors errors, List<Integer> coursePlaceOrders) {
        Set<Integer> duplicatePlaceOrders = coursePlaceOrders.stream()
                .filter(placeOrder -> Collections.frequency(coursePlaceOrders, placeOrder) > 1)
                .collect(Collectors.toSet());

        if (!duplicatePlaceOrders.isEmpty()) {
            errors.reject("Duplicate", new String[]{"order"}, null);
        }
    }
}
