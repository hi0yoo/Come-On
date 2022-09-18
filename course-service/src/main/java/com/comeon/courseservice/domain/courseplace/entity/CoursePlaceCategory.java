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

    private final String description;

    CoursePlaceCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static CoursePlaceCategory of(String name) {
        for (CoursePlaceCategory placeCategory : CoursePlaceCategory.values()) {
            if (placeCategory.name().equalsIgnoreCase(name)) {
                return placeCategory;
            }
        }
        return null;
    }
}
