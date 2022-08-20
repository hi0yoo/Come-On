package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;

@Getter
public class UserDetailResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;

    public UserDetailResponse(UserDto userDto) {
        this.userId = userDto.getId();
        this.nickname = userDto.getNickname();
        this.profileImgUrl = userDto.getProfileImgUrl();
    }
}
