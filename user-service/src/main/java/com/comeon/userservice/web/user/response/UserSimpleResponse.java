package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserSimpleResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;

    public UserSimpleResponse(User user, String profileImgUrl) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImgUrl = profileImgUrl;
    }
}
