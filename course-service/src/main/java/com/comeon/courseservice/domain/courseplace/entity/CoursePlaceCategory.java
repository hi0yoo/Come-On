package com.comeon.courseservice.domain.courseplace.entity;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;

public enum CoursePlaceCategory {

    SCHOOL("학교"),
    SHOPPING("쇼핑"),
    STATION("지하철역"),
    ATTRACTION("관광명소"),
    CULTURE("문화시설"),
    ACTIVITY("액티비티"),
    ACCOMMODATION("숙박"),
    RESTAURANT("음식점"),
    BAR("술집"),
    CAFE("카페"),
    SPORT("스포츠/레저"),
    ETC("기타"),
    ;

    private final String categoryName;

    CoursePlaceCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static CoursePlaceCategory of(String categoryName) {
        for (CoursePlaceCategory placeCategory : CoursePlaceCategory.values()) {
            if (placeCategory.getCategoryName().equals(categoryName)
                    || placeCategory.name().equalsIgnoreCase(categoryName)) {
                return placeCategory;
            }
        }
        throw new CustomException("해당 카테고리는 지원하지 않습니다. 요청한 카테고리 이름 : " + categoryName, ErrorCode.VALIDATION_FAIL);
    }
}
