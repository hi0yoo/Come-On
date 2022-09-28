package com.comeon.courseservice.web.course.response;

import lombok.Getter;

@Getter
public class UserDetailInfo {

    private Long id;
    private String nickname;

    public UserDetailInfo(Long userId, String nickname) {
        this.id = userId;
        this.nickname = nickname;
    }
}
