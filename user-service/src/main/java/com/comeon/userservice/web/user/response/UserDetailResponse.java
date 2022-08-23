package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;

@Getter
public class UserDetailResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;

    private String email;
    private String name;

    public UserDetailResponse(UserDto userDto, String profileImgUrl) {
        this.userId = userDto.getId();
        this.nickname = userDto.getNickname();
        this.profileImgUrl = profileImgUrl;
        this.email = userDto.getAccountDto().getEmail();
        this.name = userDto.getAccountDto().getName();
    }
}
