package com.comeon.userservice.web.feign.authservice.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class UnlinkResponse {
    private String message;

    public UnlinkResponse(String message) {
        this.message = message;
    }
}
