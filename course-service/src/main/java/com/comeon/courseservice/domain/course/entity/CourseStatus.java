package com.comeon.courseservice.domain.course.entity;

public enum CourseStatus {

    WRITING("아직 작성중인 코스입니다."),
    COMPLETE("작성이 완료된 코스입니다."),
    DISABLED("비활성화 된 코스입니다."),
    ;

    private final String description;

    CourseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static CourseStatus of(String name) {
        for (CourseStatus courseStatus : CourseStatus.values()) {
            if (courseStatus.name().equalsIgnoreCase(name)) {
                return courseStatus;
            }
        }
        return null;
    }
}
