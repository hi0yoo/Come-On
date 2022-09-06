package com.comeon.courseservice.web.course.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
검색 조건 - 최신순, 위치 가까운 순, 좋아요 많은 순, 코스 제목 검색,
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseCondition {

    // 제목
    private String title;

    // 사용자 위도
    private Double lat;

    // 사용자 경도
    private Double lng;
}
