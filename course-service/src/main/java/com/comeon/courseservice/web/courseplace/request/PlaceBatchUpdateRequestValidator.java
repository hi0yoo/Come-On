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
        List<CoursePlaceModifyRequest> modifyRequests = request.getToModify();
        List<CoursePlaceDeleteRequest> deleteRequests = request.getToDelete();

        List<Long> coursePlaceIds = new ArrayList<>();
        List<Integer> coursePlaceOrders = new ArrayList<>();

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
                            .map(CoursePlaceModifyRequest::getId)
                            .collect(Collectors.toList())
            );
            coursePlaceOrders.addAll(
                    modifyRequests.stream()
                            .map(CoursePlaceModifyRequest::getOrder)
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

        // order 1부터 시작 검증
        if (!coursePlaceOrders.isEmpty()) {
            checkOrderStart(errors, coursePlaceOrders);
        }

        // order 연속된 수 검증
        checkOrderConsecutive(errors, coursePlaceOrders);
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

    private void checkOrderStart(Errors errors, List<Integer> coursePlaceOrders) {
        if (!coursePlaceOrders.get(0).equals(1)) {
            errors.reject("OrderStart", new String[]{"order", "1"}, null);
        }
    }

    private void checkOrderConsecutive(Errors errors, List<Integer> coursePlaceOrders) {
        List<Integer> orders = coursePlaceOrders.stream().distinct().collect(Collectors.toList());
        for (int i = 0; i < orders.size() - 1; i++) {
            if (orders.get(i) + 1 != orders.get(i + 1)) {
                errors.reject("Consecutive", new String[] {"order"}, null);
                break;
            }
        }
    }
}
