package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserSimpleResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;
    private String status;

    @Builder(builderClassName = "activateUserResponseBuilder", builderMethodName = "activateUserResponseBuilder")
    public UserSimpleResponse(User user, String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
        this.userId = user.getId();
        this.status = user.getStatus().name();
        this.nickname = user.getNickname();
    }

    @Builder(builderClassName = "withdrawnUserResponseBuilder", builderMethodName = "withdrawnUserResponseBuilder")
    public UserSimpleResponse(User user) {
        this.userId = user.getId();
        this.status = user.getStatus().name();
    }

}
