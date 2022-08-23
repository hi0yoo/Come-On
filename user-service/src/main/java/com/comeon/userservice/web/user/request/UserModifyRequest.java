package com.comeon.userservice.web.user.request;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class UserModifyRequest {

    @NotBlank
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
