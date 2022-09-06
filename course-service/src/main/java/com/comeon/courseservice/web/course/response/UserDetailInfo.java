package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class UserDetailInfo {

    private Long userId;
    private String nickname;

    public UserDetailInfo(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
