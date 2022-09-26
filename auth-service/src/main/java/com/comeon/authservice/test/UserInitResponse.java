package com.comeon.authservice.test;

import com.comeon.authservice.common.jwt.JwtTokenInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInitResponse {

    private List<Data> contents;

    public UserInitResponse() {
        this.contents = new ArrayList<>();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Data {
        private Long userId;
        private JwtTokenInfo accessToken;
        private JwtTokenInfo refreshToken;

        private JwtTokenInfo expiredAccessToken;
    }
}
