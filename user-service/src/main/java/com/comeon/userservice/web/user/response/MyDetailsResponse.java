package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;

@Getter
public class MyDetailsResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;

    private String email;
    private String name;

    public MyDetailsResponse(UserDto userDto) {
        this.userId = userDto.getId();
        this.nickname = userDto.getNickname();
        this.profileImgUrl = userDto.getProfileImgUrl();
        this.email = userDto.getEmail();
        this.name = userDto.getName();
    }
}
