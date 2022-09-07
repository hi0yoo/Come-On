package com.comeon.courseservice.domain.courseplace.entity;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;

public enum CoursePlaceCategory {

    MT1("대형마트"),
    CS2("편의점"),
    PS3("어린이집, 유치원"),
    SC4("학교"),
    AC5("학원"),
    PK6("주차장"),
    OL7("주유소, 충전소"),
    SW8("지하철역"),
    BK9("은행"),
    CT1("문화시설"),
    AG2("중개업소"),
    PO3("공공기관"),
    AT4("관광명소"),
    AD5("숙박"),
    FD6("음식점"),
    CE7("카페"),
    HP8("병원"),
    PM9("약국"),
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
