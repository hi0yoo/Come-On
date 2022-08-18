package com.comeon.userservice.web.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSaveResponse {

    private Long userId;
    private String role;
}
