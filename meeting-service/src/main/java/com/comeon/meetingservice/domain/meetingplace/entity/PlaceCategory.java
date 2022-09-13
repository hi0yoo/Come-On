package com.comeon.meetingservice.domain.meetingplace.entity;

public enum PlaceCategory {

    SHOPPING("쇼핑"),
    ATTRACTION("관광명소"),
    CULTURE("문화시설"),
    ACTIVITY("액티비티"),
    ACCOMMODATION("숙박"),
    RESTAURANT("음식점"),
    BAR("술집"),
    CAFE("카페"),
    SPORT("스포츠"),
    SCHOOL("학교"),
    ETC("기타");

    private final String korName;

    PlaceCategory(String korName) {
        this.korName = korName;
    }

    public String getKorName() {
        return korName;
    }
}
