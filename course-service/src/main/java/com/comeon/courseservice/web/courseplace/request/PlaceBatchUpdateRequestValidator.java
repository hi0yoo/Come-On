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
                            .map(CoursePlaceModifyRequest::getCoursePlaceId)
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
                            .map(CoursePlaceDeleteRequest::getCoursePlaceId)
                            .collect(Collectors.toList())
            );
        }

        coursePlaceIds.sort(Comparator.comparingLong(Long::longValue));
        coursePlaceOrders.sort(Comparator.comparingInt(Integer::intValue));

        // null 제거
        coursePlaceIds.removeIf(Objects::isNull);
        for (Long coursePlaceId : coursePlaceIds) {
            System.out.println("placeId : " + coursePlaceId);
        }
        System.out.println("==============");
        coursePlaceOrders.removeIf(Objects::isNull);
        for (Integer coursePlaceOrder : coursePlaceOrders) {
            System.out.println("placeOrder : " + coursePlaceOrder);
        }

        // id 중복 검증
        // TODO null 일때도 중복 처리된다.
        Set<Long> duplicatePlaceIds = coursePlaceIds.stream()
                .filter(placeId -> Collections.frequency(coursePlaceIds, placeId) > 1)
                .collect(Collectors.toSet());

        if (!duplicatePlaceIds.isEmpty()) {
            errors.reject("Duplicate", new String[]{"coursePlaceId"}, null);
        }

        // order 중복 검증
        Set<Integer> duplicatePlaceOrders = coursePlaceOrders.stream()
                .filter(placeOrder -> Collections.frequency(coursePlaceOrders, placeOrder) > 1)
                .collect(Collectors.toSet());

        if (!duplicatePlaceOrders.isEmpty()) {
            errors.reject("Duplicate", new String[]{"order"}, null);
        }

        // order 1부터 시작 검증
        if (!coursePlaceOrders.get(0).equals(1)) {
            errors.reject("OrderStart", new String[]{"order", "1"}, null);
        }

        for (int i = 0; i < coursePlaceOrders.size() - 1; i++) {
            // order 연속된 수 검증
            if (coursePlaceOrders.get(i) + 1 != coursePlaceOrders.get(i + 1)) {
                errors.reject("Consecutive", new String[] {"order"}, null);
                break;
            }
        }
    }
}
