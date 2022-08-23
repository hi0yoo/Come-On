package com.comeon.userservice.web.user.request;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserModifyRequest {

    private String nickname;

    public UserModifyRequest(String nickname) {
        this.nickname = nickname;
    }

    public UserDto toServiceDto() {
        return UserDto.builder()
                .nickname(nickname)
                .build();
    }
}
