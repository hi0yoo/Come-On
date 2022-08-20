package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSaveResponse {

    private Long userId;
    private String role;

    public UserSaveResponse(UserDto userDto) {
        this.userId = userDto.getId();
        this.role = userDto.getRole().getRoleValue();
    }
}
