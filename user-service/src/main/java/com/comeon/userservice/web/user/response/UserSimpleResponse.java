package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;

@Getter
public class UserSimpleResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;

    public UserSimpleResponse(UserDto userDto) {
        this.userId = userDto.getId();
        this.nickname = userDto.getNickname();
        this.profileImgUrl = userDto.getProfileImgUrl();
    }
}
