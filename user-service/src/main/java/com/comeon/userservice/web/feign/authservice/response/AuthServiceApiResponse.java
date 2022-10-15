package com.comeon.userservice.web.feign.authservice.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthServiceApiResponse<T> {

    private String responseTime;
    private String code;
    private T data;
}
