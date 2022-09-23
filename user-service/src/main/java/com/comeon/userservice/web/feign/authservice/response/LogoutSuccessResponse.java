package com.comeon.userservice.web.feign.authservice.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class LogoutSuccessResponse {
    private String message;

    public LogoutSuccessResponse(String message) {
        this.message = message;
    }
}
